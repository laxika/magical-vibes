package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PsychicPuppetryTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped creature when the controller accepts")
    void tapsUntappedCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castPuppetry(bears);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped land when the controller accepts")
    void untapsTappedLand() {
        Permanent forest = addReadyLand(player1);
        forest.tap();
        castPuppetry(forest);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves the permanent untouched")
    void decliningDoesNothing() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castPuppetry(bears);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        PsychicPuppetry puppetry = new PsychicPuppetry();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneShock, puppetry));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, giant.getId(), List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(giant.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(puppetry);
    }

    private void castPuppetry(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new PsychicPuppetry()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
