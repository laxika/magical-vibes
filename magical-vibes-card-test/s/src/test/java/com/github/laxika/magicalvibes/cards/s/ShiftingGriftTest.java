package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiftingGrift.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class,
        DarksteelCitadel.class, AngelicChorus.class, GloriousAnthem.class, SterlingHound.class})
class ShiftingGriftTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control for each selected permanent type")
    void exchangesControlForEachSelectedType() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(new int[]{0, 1, 2}, List.of(
                ownCreature.getId(), opponentCreature.getId(),
                ownArtifact.getId(), opponentArtifact.getId(),
                ownEnchantment.getId(), opponentEnchantment.getId()), 6);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(opponentCreature, opponentArtifact, opponentEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(ownCreature, ownArtifact, ownEnchantment);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Rejects choosing the same permanent twice within one exchange mode")
    void rejectsDuplicateTargetsWithinOneMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(creature.getId(), creature.getId()), 4))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact mode alone uses its own two targets and costs one additional mana")
    void exchangesArtifactsWithoutChoosingCreatureMode() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        cast(new int[]{1}, List.of(own.getId(), opponent.getId()), 3);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponent).doesNotContain(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own).doesNotContain(opponent);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Enchantment mode alone uses its own two targets and costs one additional mana")
    void exchangesEnchantmentsWithoutChoosingEarlierModes() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(new int[]{2}, List.of(own.getId(), opponent.getId()), 3);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponent).doesNotContain(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own).doesNotContain(opponent);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Two permanents controlled by the opponent are legal targets but do not exchange")
    void sameControllerExchangeDoesNothing() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        cast(new int[]{0}, List.of(first.getId(), second.getId()), 4);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        harness.assertInGraveyard(player1, "Shifting Grift");
    }

    @Test
    @DisplayName("The same artifact creatures can be exchanged once by each selected mode")
    void allowsSharingTargetsAcrossModes() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SterlingHound());

        cast(new int[]{0, 1}, List.of(own.getId(), opponent.getId(), own.getId(), opponent.getId()), 5);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own).doesNotContain(opponent);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent).doesNotContain(own);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Later modes use controllers resulting from earlier exchanges")
    void laterExchangeUsesCurrentControllers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new SterlingHound());

        cast(new int[]{0, 1}, List.of(first.getId(), second.getId(), first.getId(), third.getId()), 5);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second).doesNotContain(third);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(third).doesNotContain(first, second);
    }

    @Test
    @DisplayName("Losing one creature target prevents only that exchange, leaving the artifact exchange intact")
    void missingTargetDoesNotPreventOtherMode() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.setHand(player1, List.of(new ShiftingGrift()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1},
                List.of(ownCreature.getId(), opponentCreature.getId(),
                        ownArtifact.getId(), opponentArtifact.getId()), null);

        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerGraveyards.get(player1.getId()).add(ownCreature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentArtifact)
                .doesNotContain(opponentCreature, ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature, ownArtifact)
                .doesNotContain(opponentArtifact);
        harness.assertInGraveyard(player1, "Shifting Grift");
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new ShiftingGrift()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 2);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, modes, targets, null);
        harness.passBothPriorities();
    }
}
