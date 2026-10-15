package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.EssenceInfusion;
import com.github.laxika.magicalvibes.cards.e.ExponentialGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CodieVociferousCodex.class, DarkRitual.class, Divination.class, EssenceInfusion.class,
        ExponentialGrowth.class, GrizzlyBears.class, Shock.class})
class CodieVociferousCodexTest extends BaseCardTest {

    @Test
    @DisplayName("Codie prevents its controller from casting permanent spells")
    void preventsPermanentSpells() {
        addCodie();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Codie's ability adds one mana of each color")
    void addsOneManaOfEachColor() {
        Permanent codie = addCodie();
        activateCodie(codie);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(codie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Codie's delayed cascade still triggers after Codie leaves the battlefield")
    void delayedCascadeSurvivesSourceLeaving() {
        Permanent codie = addCodie();
        activateCodie(codie);
        gd.playerBattlefields.get(player1.getId()).remove(codie);

        harness.setHand(player1, List.of(new Divination()));
        DarkRitual hit = new DarkRitual();
        harness.setLibrary(player1, List.of(hit));
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(hit);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Codie cascades from the next instant or sorcery using that spell's mana value")
    void cascadesFromNextSpell() {
        Permanent codie = addCodie();
        activateCodie(codie);

        harness.setHand(player1, List.of(new DarkRitual()));
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Codie offers a lesser instant or sorcery for free and only triggers once")
    void offersLesserInstantOrSorceryOnce() {
        Permanent codie = addCodie();
        activateCodie(codie);

        harness.setHand(player1, List.of(new Divination()));
        DarkRitual hit = new DarkRitual();
        DarkRitual secondHit = new DarkRitual();
        harness.setLibrary(player1, List.of(hit, new Shock(), new GrizzlyBears(), secondHit));
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(hit);

        gd.playerManaPools.get(player1.getId()).clear();
        harness.castFromExile(player1, hit.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondHit);
    }

    @Test
    @DisplayName("Codie leaves an uncast hit in exile and bottoms only the other examined cards")
    void leavesUncastHitInExile() {
        Permanent codie = addCodie();
        activateCodie(codie);
        harness.setHand(player1, List.of(new Divination()));
        GrizzlyBears skipped = new GrizzlyBears();
        DarkRitual hit = new DarkRitual();
        Shock unexamined = new Shock();
        harness.setLibrary(player1, List.of(skipped, hit, unexamined));

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unexamined, skipped);

        harness.setLibrary(player2, List.of(new Shock()));
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(hit);
        assertThatThrownBy(() -> harness.castFromExile(player1, hit.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A sorcery exiled by Codie must wait until the stack is empty")
    void exiledSorceryUsesNormalTiming() {
        Permanent codie = addCodie();
        activateCodie(codie);
        harness.setHand(player1, List.of(new Divination()));
        EssenceInfusion hit = new EssenceInfusion();
        harness.setLibrary(player1, List.of(hit, new Shock(), new GrizzlyBears()));

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(hit);
        assertThatThrownBy(() -> harness.castFromExile(player1, hit.getId(), codie.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.castFromExile(player1, hit.getId(), codie.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hit);
    }

    @Test
    @DisplayName("Codie's mana-value threshold includes every X symbol in the triggering spell")
    void triggeringSpellManaValueIncludesX() {
        Permanent codie = addCodie();
        activateCodie(codie);
        harness.setHand(player1, List.of(new ExponentialGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        Divination hit = new Divination();
        harness.setLibrary(player1, List.of(hit));

        harness.castSorcery(player1, 0, 2, codie.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's spell does not consume Codie's delayed trigger")
    void ignoresOpponentsSpell() {
        Permanent codie = addCodie();
        activateCodie(codie);
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        DarkRitual hit = new DarkRitual();
        harness.setLibrary(player1, List.of(hit));

        harness.castInstant(player2, 0, (UUID) null);
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);

        harness.setHand(player1, List.of(new Divination()));
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(hit);
    }

    @Test
    @DisplayName("An unused Codie delayed trigger expires at end of turn")
    void unusedTriggerExpiresAtEndOfTurn() {
        Permanent codie = addCodie();
        activateCodie(codie);
        harness.setLibrary(player1, List.of(new Divination()));
        harness.setLibrary(player2, List.of(new Shock()));
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Shock libraryCard = new Shock();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.castInstant(player1, 0, (UUID) null);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Codie's activated ability resolves immediately without using the stack")
    void activatedAbilityIsAManaAbility() {
        Permanent codie = addCodie();
        activateCodie(codie);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Codie does not prevent an opponent from casting permanent spells")
    void opponentCanCastPermanentSpells() {
        addCodie();
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addCodie() {
        return addCreatureReady(player1, new CodieVociferousCodex());
    }

    private void activateCodie(Permanent codie) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(codie), null, null);
        harness.clearPriorityPassed();
    }
}
