package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RamsesAssassinLord.class)
class RamsesAssassinLordTest extends BaseCardTest {

    @Test
    @DisplayName("Other Assassins you control get +1/+1")
    void boostsOtherAssassinsYouControl() {
        Permanent assassin = addCreatureReady(player1, creature("Assassin", CardSubtype.ASSASSIN));
        Permanent ramses = addCreatureReady(player1, new RamsesAssassinLord());

        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, assassin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ramses)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ramses)).isEqualTo(4);
    }

    @Test
    @DisplayName("The win trigger resolves only after an Assassin you control attacked the losing player")
    void winsOnlyForQualifyingAssassinAttack() {
        Permanent ramses = addCreatureReady(player1, new RamsesAssassinLord());
        Permanent assassin = addCreatureReady(player1, creature("Assassin", CardSubtype.ASSASSIN));
        gd.recordAttackAgainstPlayer(assassin.getId(), player2.getId());

        resolveLossTrigger(ramses);

        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A non-Assassin attack does not satisfy the win trigger")
    void nonAssassinAttackDoesNotWin() {
        Permanent ramses = addCreatureReady(player1, new RamsesAssassinLord());
        Permanent attacker = addCreatureReady(player1, creature("Rogue", CardSubtype.ROGUE));
        gd.recordAttackAgainstPlayer(attacker.getId(), player2.getId());

        resolveLossTrigger(ramses);

        assertThat(gd.gameResult).isNull();
    }

    @Test
    @DisplayName("The loss trigger carries the losing player as event context")
    void lossTriggerCarriesLosingPlayerContext() {
        Permanent ramses = addCreatureReady(player1, new RamsesAssassinLord());
        harness.setLife(player2, 0);
        harness.runStateBasedActions();

        StackEntry trigger = gd.stack.stream()
                .filter(entry -> entry.getSourcePermanentId() != null
                        && entry.getSourcePermanentId().equals(ramses.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(trigger.getTargetId()).isEqualTo(player2.getId());
    }

    private void resolveLossTrigger(Permanent source) {
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                player1.getId(),
                "Ramses, Assassin Lord's triggered ability",
                List.of(new WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect(CardSubtype.ASSASSIN)),
                player2.getId(),
                source.getId());
        trigger.setNonTargeting(true);
        gd.stack.add(trigger);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLACK);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
