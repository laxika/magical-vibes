package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VernalSovereign.class})
class VernalSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a green and white Elemental whose P/T equals the creature count")
    void etbCreatesCreatureCountElemental() {
        castAndResolveSovereign();

        Permanent token = findElementalToken();
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @CardUsed({GrizzlyBears.class})
    @DisplayName("Elemental P/T updates as creatures enter and ignores opponent creatures")
    void tokenPowerToughnessTracksControlledCreatures() {
        castAndResolveSovereign();

        Permanent token = findElementalToken();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking creates a creature-count Elemental token")
    void attackCreatesCreatureCountElemental() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new VernalSovereign());
        sovereign.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        Permanent token = findElementalToken();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Elemental survives without its source and counts itself")
    void tokenCountsItselfAfterSourceLeaves() {
        castAndResolveSovereign();
        Permanent token = findElementalToken();
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> !permanent.getCard().isToken());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(token);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Elemental counts its current controller's creatures")
    void tokenCountsNewControllersCreatures() {
        castAndResolveSovereign();
        Permanent token = findElementalToken();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);
        harness.addToBattlefield(player2, new VernalSovereign());
        harness.addToBattlefield(player2, new VernalSovereign());

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack creates a second Elemental and both count each other")
    void attackUpdatesExistingAndNewTokens() {
        castAndResolveSovereign();
        Permanent sovereign = gd.playerBattlefields.get(player1.getId()).getFirst();
        sovereign.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
        }
    }

    private void castAndResolveSovereign() {
        harness.setHand(player1, List.of(new VernalSovereign()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findElementalToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Elemental"))
                .findFirst()
                .orElseThrow();
    }
}
