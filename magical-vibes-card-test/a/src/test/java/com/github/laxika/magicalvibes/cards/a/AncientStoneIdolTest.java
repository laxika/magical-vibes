package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientStoneIdol.class})
class AncientStoneIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each opponent's attacking creature")
    void costIsReducedForEachAttackingCreature() {
        Permanent attacker1 = addCreatureReady(player2, new AncientStoneIdol());
        Permanent attacker2 = addCreatureReady(player2, new AncientStoneIdol());
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.setHand(player1, List.of(new AncientStoneIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Only attacking creatures reduce the cost, including your own attackers")
    void nonattackingCreaturesDoNotReduceCost() {
        Permanent attacker = addCreatureReady(player1, new AncientStoneIdol());
        attacker.setAttacking(true);
        addCreatureReady(player1, new AncientStoneIdol());
        addCreatureReady(player2, new AncientStoneIdol());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new AncientStoneIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("More than ten attackers reduce the cost to zero")
    void costCannotBecomeNegative() {
        for (int i = 0; i < 11; i++) {
            addCreatureReady(player1, new AncientStoneIdol()).setAttacking(true);
        }
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new AncientStoneIdol()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step at full cost")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new AncientStoneIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ancient Stone Idol");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Exiling the Idol does not create a Construct")
    void exileDoesNotTriggerDeathAbility() {
        Permanent idol = addCreatureReady(player1, new AncientStoneIdol());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, idol));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ancient Stone Idol");
        assertThat(countPermanents(player1, "Construct")).isZero();
    }

    @Test
    @DisplayName("Creates a trampling Construct token when it dies")
    void createsConstructWhenItDies() {
        Permanent idol = addCreatureReady(player1, new AncientStoneIdol());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, idol));
        harness.passBothPriorities();

        List<Permanent> constructs = findPermanents(player1, "Construct");
        assertThat(constructs).hasSize(1);

        Permanent construct = constructs.getFirst();
        assertThat(construct.getCard().getPower()).isEqualTo(6);
        assertThat(construct.getCard().getToughness()).isEqualTo(12);
        assertThat(construct.getCard().getColor()).isNull();
        assertThat(construct.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(construct.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(construct.getCard().getSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
        assertThat(construct.getCard().getKeywords()).containsExactly(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("The controller at death receives the token even when the owner is different")
    void deathTriggerUsesControllerRatherThanOwner() {
        Permanent idol = addCreatureReady(player2, new AncientStoneIdol());
        gd.stolenCreatures.put(idol.getId(), player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, idol));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Construct")).isEqualTo(1);
        assertThat(countPermanents(player1, "Construct")).isZero();
        harness.assertInGraveyard(player1, "Ancient Stone Idol");
    }
}
