package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MeteorGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LanternOfRevealing.class, Forest.class, MeteorGolem.class})
class LanternOfRevealingTest extends BaseCardTest {

    @Test
    void producesManaOfAnyColor() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new LanternOfRevealing());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(lantern.isTapped()).isTrue();
    }

    @Test
    void putsTopLandOntoBattlefieldTapped() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new LanternOfRevealing());
        Forest forest = new Forest();
        MeteorGolem nonland = new MeteorGolem();
        harness.setLibrary(player1, List.of(forest, nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(lantern.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped());
    }

    @Test
    void declinedLandMayBePutOnBottom() {
        harness.addToBattlefield(player1, new LanternOfRevealing());
        Forest forest = new Forest();
        MeteorGolem nonland = new MeteorGolem();
        harness.setLibrary(player1, List.of(forest, nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland, forest);
    }

    @Test
    void nonlandMayBePutOnBottom() {
        harness.addToBattlefield(player1, new LanternOfRevealing());
        MeteorGolem nonland = new MeteorGolem();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(nonland, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, nonland);
    }

    @Test
    void controllerCanIdentifyNonlandBeforeChoosingWhetherToBottomIt() {
        harness.addToBattlefield(player1, new LanternOfRevealing());
        MeteorGolem nonland = new MeteorGolem();
        harness.setLibrary(player1, List.of(nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("Meteor Golem"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Meteor Golem"));
    }

    @Test
    void decliningBothChoicesLeavesLandOnTop() {
        harness.addToBattlefield(player1, new LanternOfRevealing());
        Forest forest = new Forest();
        MeteorGolem nonland = new MeteorGolem();
        harness.setLibrary(player1, List.of(forest, nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, nonland);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void decliningBottomChoiceLeavesNonlandOnTop() {
        harness.addToBattlefield(player1, new LanternOfRevealing());
        MeteorGolem nonland = new MeteorGolem();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(nonland, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland, forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void emptyLibraryResolvesWithoutOfferingChoices() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new LanternOfRevealing());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(lantern.isTapped()).isTrue();
    }
}
