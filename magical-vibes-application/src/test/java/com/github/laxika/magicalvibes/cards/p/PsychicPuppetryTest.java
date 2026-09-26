package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.cards.o.OkinaTempleToTheGrandfathers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychicPuppetry.class, GlacialRay.class, KondaLordOfEiganjo.class,
        OkinaTempleToTheGrandfathers.class})
class PsychicPuppetryTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped creature when the controller accepts")
    void tapsUntappedCreature() {
        Permanent konda = addCreatureReady(player2, new KondaLordOfEiganjo());
        castPuppetry(konda);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(konda.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped land when the controller accepts")
    void untapsTappedLand() {
        Permanent okina = addReadyLand(player1);
        okina.tap();
        castPuppetry(okina);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(okina.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves the permanent untouched")
    void decliningDoesNothing() {
        Permanent konda = addCreatureReady(player2, new KondaLordOfEiganjo());
        castPuppetry(konda);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(konda.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent konda = addCreatureReady(player2, new KondaLordOfEiganjo());
        GlacialRay arcaneRay = new GlacialRay();
        PsychicPuppetry puppetry = new PsychicPuppetry();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneRay, puppetry));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, konda.getId(), List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(konda.isTapped()).isTrue();
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
        return harness.addToBattlefieldAndReturn(player, new OkinaTempleToTheGrandfathers());
    }
}
