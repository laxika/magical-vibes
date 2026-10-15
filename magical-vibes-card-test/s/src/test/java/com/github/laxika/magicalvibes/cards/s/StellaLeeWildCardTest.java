package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StellaLeeWildCard.class, DarkRitual.class, Fireball.class, GrizzlyBears.class,
        LightningBolt.class, Mountain.class})
class StellaLeeWildCardTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card on your second spell and lets you play it until your next turn ends")
    void exilesTopCardOnSecondSpell() {
        Card top = new LightningBolt();
        harness.setLibrary(player1, List.of(top));
        addCreatureReady(player1, new StellaLeeWildCard());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
    }

    @Test
    void triggersOnlyOnSecondSpellAndCanCastExiledCardAtNormalCost() {
        LightningBolt top = new LightningBolt();
        Mountain remaining = new Mountain();
        harness.setLibrary(player1, List.of(top, remaining));
        addCreatureReady(player1, new StellaLeeWildCard());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        harness.castFromExile(player1, top.getId(), player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
    }

    @Test
    void mayPlayExiledLandAfterSecondSpellResolves() {
        Mountain top = new Mountain();
        harness.setLibrary(player1, List.of(top));
        addCreatureReady(player1, new StellaLeeWildCard());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.castFromExile(player1, top.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void permissionLastsThroughNextTurnAndCardRemainsExiledAfterExpiry() {
        LightningBolt top = new LightningBolt();
        harness.setLibrary(player1, List.of(top, new Mountain(), new Mountain(), new Mountain()));
        addCreatureReady(player1, new StellaLeeWildCard());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsStellaItselfAsFirstSpell() {
        Mountain top = new Mountain();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new StellaLeeWildCard(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
    }

    @Test
    void ignoresOpponentSpellsAndTriggersDuringOpponentTurn() {
        Mountain top = new Mountain();
        harness.setLibrary(player1, List.of(top, new Mountain(), new Mountain()));
        addCreatureReady(player1, new StellaLeeWildCard());
        harness.setHand(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    void canCopyThirdSpellAndChooseNewTargetWithoutChangingOriginal() {
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);
        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        LightningBolt target = new LightningBolt();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stella),
                0, null, target.getId());
        assertThat(stella.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof LightningBolt).containsExactly(target);
    }

    @Test
    void copiesSorceryPreservingXAndMayKeepOriginalTarget() {
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0);
        }
        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        Fireball target = new Fireball();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stella),
                0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.passBothPriorities();
        harness.assertLife(player2, 12);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(4);
    }

    @Test
    void cannotTargetOpponentSpell() {
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0);
        }
        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        DarkRitual target = new DarkRitual();
        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stella), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(stella.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Copies a controlled instant or sorcery after three spells have been cast")
    void copiesControlledSpellAfterThreeSpells() {
        DarkRitual target = new DarkRitual();
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual(), target));
        harness.addMana(player1, ManaColor.BLACK, 1);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0);
        }

        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        harness.castInstant(player1, 0);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(stella),
                0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(12);
    }

    @Test
    @DisplayName("Cannot activate before three spells have been cast")
    void cannotActivateBeforeThreeSpells() {
        DarkRitual target = new DarkRitual();
        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stella),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three or more spells");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0);
        }

        Permanent stella = addCreatureReady(player1, new StellaLeeWildCard());
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(stella),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
