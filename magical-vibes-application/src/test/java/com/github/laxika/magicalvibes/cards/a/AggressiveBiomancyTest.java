package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MerchantOfSecrets;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AggressiveBiomancy.class, GrizzlyBears.class, HillGiant.class,
        MerchantOfSecrets.class, Unsummon.class})
class AggressiveBiomancyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X token copies of the targeted creature")
    void createsXTokenCopies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AggressiveBiomancy()));
        addManaForX(2);

        harness.castAndResolveSorcery(player1, 0, 2, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Each token fights up to one opposing creature when it enters")
    void eachTokenFightsOpposingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new AggressiveBiomancy()));
        addManaForX(2);

        harness.castAndResolveSorcery(player1, 0, 2, bears.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can create the copies without choosing a fight target")
    void canDeclineFightTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new AggressiveBiomancy()));
        addManaForX(1);

        harness.castAndResolveSorcery(player1, 0, 1, bears.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("X zero creates no tokens or fight triggers")
    void zeroCreatesNoTokens() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AggressiveBiomancy()));
        addManaForX(0);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bears);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Copies are created when there are no creatures to fight")
    void createsCopiesWithoutOpposingCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AggressiveBiomancy()));
        addManaForX(2);

        harness.castAndResolveSorcery(player1, 0, 2, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("No copies are created if the spell's target leaves before resolution")
    void missingCopyTargetCreatesNoTokens() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AggressiveBiomancy()));
        harness.setHand(player2, List.of(new Unsummon()));
        addManaForX(2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 2, List.of(bears.getId()));
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A copied enter ability still resolves when the fight target leaves")
    void copiedEnterAbilityIsIndependentOfFightTrigger() {
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new MerchantOfSecrets());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new AggressiveBiomancy()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManaForX(1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 1, merchant.getId());
        harness.handlePermanentChosen(player1, giant.getId());
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    private void addManaForX(int x) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2 * x);
    }
}
