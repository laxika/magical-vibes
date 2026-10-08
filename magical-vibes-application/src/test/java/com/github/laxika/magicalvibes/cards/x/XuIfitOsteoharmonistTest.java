package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XuIfitOsteoharmonist.class, ElvishVisionary.class, GrizzlyBears.class,
        HolyDay.class, ZuranSpellcaster.class})
class XuIfitOsteoharmonistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature as a Skeleton without its abilities")
    void returnsCreatureAsSkeletonWithoutAbilities() {
        Card creature = new ZuranSpellcaster();
        addReadyXuIfit();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Zuran Spellcaster");
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned)).contains(CardSubtype.SKELETON);
        returned.setSummoningSick(false);
        int returnedIndex = gd.playerBattlefields.get(player1.getId()).indexOf(returned);

        assertThatThrownBy(() -> harness.activateAbility(player1, returnedIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Suppresses the returned creature's enter-the-battlefield ability")
    void suppressesReturnedCreatureEtbAbility() {
        Card creature = new ElvishVisionary();
        Card libraryCard = new GrizzlyBears();
        addReadyXuIfit();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
    }

    @Test
    @DisplayName("Can target only a creature card in the controller's graveyard")
    void targetsOnlyCreatureCards() {
        addReadyXuIfit();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void canActivateOnlyAtSorcerySpeed() {
        addReadyXuIfit();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        addReadyXuIfit();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("The tap cost cannot be paid with a summoning-sick Xu-Ifit")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new XuIfitOsteoharmonist());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The tap cost cannot be paid with a tapped Xu-Ifit")
    void cannotActivateWhileTapped() {
        addReadyXuIfit();
        findPermanent(player1, "Xu-Ifit, Osteoharmonist").tap();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during an opponent's main phase")
    void cannotActivateOnOpponentsTurn() {
        addReadyXuIfit();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability resolves after Xu-Ifit leaves and preserves the creature's other types")
    void resolvesAfterSourceLeavesBattlefield() {
        addReadyXuIfit();
        Permanent source = findPermanent(player1, "Xu-Ifit, Osteoharmonist");
        Card creature = new ZuranSpellcaster();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        assertThat(source.isTapped()).isTrue();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Zuran Spellcaster");
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned))
                .contains(CardSubtype.SKELETON, CardSubtype.HUMAN, CardSubtype.WIZARD);
        assertThat(returned.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Zuran Spellcaster");
        returned.setSummoningSick(false);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Does not return a target that has left the graveyard before resolution")
    void doesNotReturnMissingTarget() {
        addReadyXuIfit();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, creature.getId());
        harness.setHand(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyXuIfit() {
        addCreatureReady(player1, new XuIfitOsteoharmonist());
    }
}
