package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
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

@CardUsed({MakeAStand.class, GreenwoodSentinel.class, LightningStrike.class})
class MakeAStandTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives own creatures +1/+0 and indestructible")
    void boostsAndGrantsIndestructible() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        for (Permanent p : creaturesOf(player1.getId())) {
            assertThat(p.getEffectivePower()).isEqualTo(3);
            assertThat(p.getEffectiveToughness()).isEqualTo(2);
            assertThat(p.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        }
    }

    @Test
    @DisplayName("Opponent's creatures are unaffected")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        for (Permanent p : creaturesOf(player2.getId())) {
            assertThat(p.getEffectivePower()).isEqualTo(2);
            assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        }
    }

    @Test
    @DisplayName("A boosted creature survives lethal damage")
    void indestructibleCreatureSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creaturesOf(player1.getId()).getFirst().getId());

        assertThat(creaturesOf(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost and indestructible wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        for (Permanent p : creaturesOf(player1.getId())) {
            assertThat(p.getPowerModifier()).isEqualTo(0);
            assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        }
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither effect")
    void doesNotAffectCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        Permanent original = creaturesOf(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        Permanent newcomer = creaturesOf(player1.getId()).getLast();

        assertThat(original.getEffectivePower()).isEqualTo(3);
        assertThat(original.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(newcomer.getEffectivePower()).isEqualTo(2);
        assertThat(newcomer.getEffectiveToughness()).isEqualTo(2);
        assertThat(newcomer.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Creatures entering while the spell is on the stack receive both effects")
    void affectsCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);

        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.passBothPriorities();

        Permanent creature = creaturesOf(player1.getId()).getFirst();
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("The spell resolves with no creatures and does not affect later creatures")
    void resolvesWithNoCreatures() {
        harness.setHand(player1, List.of(new MakeAStand()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Make a Stand");
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        Permanent creature = creaturesOf(player1.getId()).getFirst();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    private List<Permanent> creaturesOf(java.util.UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .toList();
    }
}
