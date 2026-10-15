package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CratersClaws;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TibaltCosmicImpostor;
import com.github.laxika.magicalvibes.cards.t.TreasureCruise;
import com.github.laxika.magicalvibes.cards.v.ValkiGodOfLies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NarsetEnlightenedMaster.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class})
class NarsetEnlightenedMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles the top four cards and grants free permission only to noncreature spells")
    void attackingExilesTopFourAndGrantsFreePermissionToNoncreatureSpells() {
        Card spell = new Divination();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card otherSpell = new Shock();
        harness.setLibrary(player1, List.of(spell, creature, land, otherSpell));
        addCreatureReady(player1, new NarsetEnlightenedMaster());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(spell.getId(), creature.getId(), land.getId(), otherSpell.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(spell.getId(), player1.getId())
                .containsEntry(otherSpell.getId(), player1.getId())
                .doesNotContainKeys(creature.getId(), land.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(spell.getId(), otherSpell.getId())
                .doesNotContain(creature.getId(), land.getId());
    }

    @Test
    @DisplayName("A permitted spell can be cast from exile without mana")
    void permittedSpellCanBeCastFromExileWithoutMana() {
        Card spell = new Divination();
        harness.setLibrary(player1, List.of(
                spell, new GrizzlyBears(), new Forest(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new NarsetEnlightenedMaster());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(spell.getId());
    }

    @Test
    @DisplayName("A short library exiles only the available cards without causing a loss")
    void shortLibraryExilesOnlyAvailableCards() {
        Card spell = new Shock();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(spell, land));
        addCreatureReady(player1, new NarsetEnlightenedMaster());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(spell, land);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A sorcery exiled by Narset still cannot be cast during combat")
    void sorceryCannotBeCastDuringCombat() {
        Card spell = new Divination();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new NarsetEnlightenedMaster());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.exilePlayPermissions).containsEntry(spell.getId(), player1.getId());
    }

    @Test
    @DisplayName("An instant exiled by Narset can be cast during combat without mana")
    void instantCanBeCastDuringCombat() {
        Card spell = new Shock();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new NarsetEnlightenedMaster());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            int lifeBefore = gd.getLife(player2.getId());

            harness.castFromExile(player1, spell.getId(), player2.getId());
            harness.passBothPriorities();

            assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        });

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Uncast cards stay exiled but cannot be cast on the next turn")
    void permissionExpiresAtEndOfTurn() {
        Card spell = new Shock();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new NarsetEnlightenedMaster());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(spell.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(spell.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({TreasureCruise.class})
    @DisplayName("Narset can cast Treasure Cruise without mana or exiling cards for delve")
    void delveSpellCanBeCastWithoutPayingItsManaCost() {
        Card spell = new TreasureCruise();
        Card draw1 = new Forest();
        Card draw2 = new Forest();
        Card draw3 = new Forest();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest(), new Forest(),
                draw1, draw2, draw3));
        addCreatureReady(player1, new NarsetEnlightenedMaster());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw1, draw2, draw3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @CardUsed({ValkiGodOfLies.class, TibaltCosmicImpostor.class})
    @DisplayName("A creature front face does not exclude a noncreature modal back face")
    void creatureFrontWithNoncreatureBackReceivesFreeCastPermission() {
        Card modalCard = new ValkiGodOfLies();
        harness.setLibrary(player1, List.of(modalCard, new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new NarsetEnlightenedMaster());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(modalCard);
        assertThat(gd.exilePlayPermissions).containsEntry(modalCard.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(modalCard.getId());
    }

    @Test
    @CardUsed({CratersClaws.class})
    @DisplayName("X must be zero when Narset casts an X spell without paying its mana cost")
    void cannotChooseNonzeroXForFreeSpell() {
        Card spell = new CratersClaws();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new NarsetEnlightenedMaster());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, spell.getId(), 5, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @CardUsed({CratersClaws.class})
    @DisplayName("A free Crater's Claws with X zero deals no damage without ferocious")
    void zeroXSpellCanBeCastForFree() {
        Card spell = new CratersClaws();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new NarsetEnlightenedMaster());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
