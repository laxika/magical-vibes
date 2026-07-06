package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessControlsPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SulfurFallsTest extends BaseCardTest {

    // ===== Enters tapped (no qualifying lands) =====

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Swamp)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isTrue();
    }

    // ===== Enters untapped (qualifying lands present) =====

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Mountain")
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both an Island and a Mountain")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isFalse();
    }

    // ===== Only checks your lands, not opponent's =====

    @Test
    @DisplayName("Opponent's Island does not satisfy the check")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player2, new Island());

        harness.setHand(player1, List.of(new SulfurFalls()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent falls = findFalls(player1);
        assertThat(falls.isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addFallsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addFallsReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addFallsReady(Player player) {
        Permanent perm = new Permanent(new SulfurFalls());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findFalls(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sulfur Falls"))
                .findFirst().orElseThrow();
    }
}
