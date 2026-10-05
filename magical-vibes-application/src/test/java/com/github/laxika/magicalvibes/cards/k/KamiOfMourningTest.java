package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfMourning.class, GrizzlyBears.class, HillGiant.class, MishrasFactory.class})
class KamiOfMourningTest extends BaseCardTest {

    @Test
    @DisplayName("Targets a creature you control or a creature card in your graveyard")
    void offersOnlyControlledCreatureZones() {
        Permanent controlledCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card controlledGraveyardCreature = new GrizzlyBears();
        Card opponentGraveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(controlledGraveyardCreature));
        harness.setGraveyard(player2, List.of(opponentGraveyardCreature));

        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(controlledCreature.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId());
        assertThat(choice.validCardIds()).containsExactly(controlledGraveyardCreature.getId());
        assertThat(choice.validCardIds()).doesNotContain(opponentGraveyardCreature.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .hasMessageContaining("Too few targets");
    }

    @Test
    void returnsASelectedGraveyardCreatureAfterAGreaterManaValueCreatureDies() {
        Card targetCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(targetCard.getId()));
        resolveAllTriggers();

        Permanent sameManaValueCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        kill(sameManaValueCreature);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(targetCard);

        Permanent greaterManaValueCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        kill(greaterManaValueCreature);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(targetCard.getId())
                        && permanent.isTapped());
    }

    @Test
    void aBattlefieldTargetKeepsTheAbilityAfterItDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        kill(target);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getOriginalCard().getId()));

        Permanent greaterManaValueCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        kill(greaterManaValueCreature);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(target.getOriginalCard().getId())
                        && permanent.isTapped());
    }

    @Test
    void repeatedGrantsTriggerIndependently() {
        Card targetCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(targetCard));
        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
            harness.passBothPriorities();
            harness.handleMultiplePermanentsChosen(player1, List.of(targetCard.getId()));
            resolveAllTriggers();
        }

        Permanent greaterManaValueCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        kill(greaterManaValueCreature);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getOriginalCard().getId().equals(targetCard.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    void opponentCreatureDeathDoesNotReturnTheCard() {
        Card targetCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(targetCard.getId()));
        resolveAllTriggers();

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        kill(opponentCreature);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(targetCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getId().equals(targetCard.getId()));
    }

    @Test
    void perpetualAbilitySurvivesReturningAndDyingAgain() {
        Card targetCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(targetCard.getId()));
        resolveAllTriggers();

        for (int i = 0; i < 2; i++) {
            Permanent greaterManaValueCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
            kill(greaterManaValueCreature);
            resolveAllTriggers();

            Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(permanent -> permanent.getOriginalCard().getId().equals(targetCard.getId()))
                    .findFirst().orElseThrow();
            assertThat(returned.isTapped()).isTrue();
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(targetCard);
            kill(returned);
            resolveAllTriggers();
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(targetCard);
        }
    }

    @Test
    void animatedLandReceivesThePerpetualAbility() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(factory.getId()));
        resolveAllTriggers();

        kill(factory);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(factory.getOriginalCard());
        Permanent greaterManaValueCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        kill(greaterManaValueCreature);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getOriginalCard().getId().equals(factory.getOriginalCard().getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    private void kill(Permanent permanent) {
        permanent.setMarkedDamage(gqs.getEffectiveToughness(gd, permanent));
        harness.runStateBasedActions();
    }
}
