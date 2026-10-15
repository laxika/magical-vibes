package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.cards.d.DourPortMage;
import com.github.laxika.magicalvibes.cards.s.ShoreUp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilypadVillage.class, DourPortMage.class, BakersbaneDuo.class, ShoreUp.class,
        BarkformHarvester.class})
class LilypadVillageTest extends BaseCardTest {

    @Test
    void tapsForColorless() {
        addVillage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void addsBlueManaOnlyForCreatureSpells() {
        addVillage();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    void creatureOnlyBlueManaCastsCreatureSpells() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new BakersbaneDuo()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void creatureOnlyBlueManaCannotCastNoncreatureSpells() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BakersbaneDuo());
        harness.setHand(player1, List.of(new ShoreUp()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void surveilRequiresQualifyingPermanentEntry() {
        addVillage();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");

        harness.enterBattlefieldAndReturn(player1, new BakersbaneDuo());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");

        harness.enterBattlefieldAndReturn(player1, new DourPortMage());
        harness.setLibrary(player1, List.of(new BakersbaneDuo(), new ShoreUp()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    void surveilsTwoCards() {
        addVillage();
        harness.enterBattlefieldAndReturn(player1, new DourPortMage());
        Card topCard = new BakersbaneDuo();
        Card secondCard = new ShoreUp();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    void creatureOnlyBlueManaPaysBlueCreatureCost() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DourPortMage()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentEntryDoesNotEnableSurveil() {
        addVillage();
        harness.enterBattlefieldAndReturn(player2, new DourPortMage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void creatureAlreadyOnBattlefieldDoesNotEnableSurveil() {
        addVillage();
        harness.addToBattlefield(player1, new DourPortMage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if");
    }

    @Test
    void changelingEntryEnablesSurveil() {
        addVillage();
        harness.enterBattlefieldAndReturn(player1, new BarkformHarvester());
        harness.setLibrary(player1, List.of(new BakersbaneDuo(), new ShoreUp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    void creatureOnlyManaCannotPaySurveilCost() {
        addVillage();
        addVillage();
        harness.enterBattlefieldAndReturn(player1, new DourPortMage());
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isFalse();
    }

    @Test
    void surveilCanReorderBothCardsOnTop() {
        addVillage();
        harness.enterBattlefieldAndReturn(player1, new DourPortMage());
        Card topCard = new BakersbaneDuo();
        Card secondCard = new ShoreUp();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilsSingleCardWhenLibraryHasOnlyOneCard() {
        addVillage();
        harness.enterBattlefieldAndReturn(player1, new DourPortMage());
        Card onlyCard = new ShoreUp();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void surveilWithEmptyLibraryCompletesWithoutInteraction() {
        addVillage();
        harness.enterBattlefieldAndReturn(player1, new DourPortMage());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void addVillage() {
        harness.addToBattlefield(player1, new LilypadVillage());
    }
}
