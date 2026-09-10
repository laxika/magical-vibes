package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DenOfTheBugbear.class, Mountain.class})
class DenOfTheBugbearTest extends BaseCardTest {

    @Test
    @DisplayName("Den of the Bugbear enters tapped with two other lands")
    void entersTappedWithTwoOtherLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        playDen();

        assertThat(findDen().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Den of the Bugbear enters untapped with fewer than two other lands")
    void entersUntappedWithFewerThanTwoOtherLands() {
        harness.addToBattlefield(player1, new Mountain());
        playDen();

        assertThat(findDen().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping Den of the Bugbear produces one red mana")
    void tappingProducesRedMana() {
        Permanent den = addDenReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(den.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Den of the Bugbear becomes a 3/2 red Goblin creature and stays a land")
    void animatesAsRedGoblin() {
        Permanent den = animateDen();

        assertThat(gqs.isCreature(gd, den)).isTrue();
        assertThat(gqs.isLand(gd, den)).isTrue();
        assertThat(gqs.getEffectivePower(gd, den)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, den)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, den)).containsExactly(CardColor.RED);
        assertThat(gqs.effectiveCreatureSubtypes(gd, den)).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Attacking with the animated Den of the Bugbear creates a tapped and attacking Goblin")
    void attackingCreatesTappedAndAttackingGoblin() {
        animateDen();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
    }

    private void playDen() {
        harness.setHand(player1, List.of(new DenOfTheBugbear()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addDenReady(Player player) {
        Permanent den = new Permanent(new DenOfTheBugbear());
        den.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(den);
        return den;
    }

    private Permanent animateDen() {
        addDenReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        return findDen();
    }

    private Permanent findDen() {
        return findPermanent(player1, "Den of the Bugbear");
    }
}
