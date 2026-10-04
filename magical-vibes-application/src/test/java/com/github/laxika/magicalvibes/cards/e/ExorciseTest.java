package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FoundFootage;
import com.github.laxika.magicalvibes.cards.f.FriendlyTeddy;
import com.github.laxika.magicalvibes.cards.i.InnocuousRat;
import com.github.laxika.magicalvibes.cards.k.KonaRescueBeastie;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTransformation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exorcise.class, FoundFootage.class, FriendlyTeddy.class, InnocuousRat.class,
        KonaRescueBeastie.class, LeylineOfTransformation.class, EnduringInnocence.class, Forest.class})
class ExorciseTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target artifact")
    void exilesArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FoundFootage());

        castExorcise(target);

        harness.assertNotOnBattlefield(player2, "Found Footage");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Found Footage"));
    }

    @Test
    @DisplayName("Exiles a target enchantment")
    void exilesEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeylineOfTransformation());

        castExorcise(target);

        harness.assertNotOnBattlefield(player2, "Leyline of Transformation");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Leyline of Transformation"));
    }

    @Test
    @DisplayName("Exiles a target creature with power 4 or greater")
    void exilesLargeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KonaRescueBeastie());

        castExorcise(target);

        harness.assertNotOnBattlefield(player2, "Kona, Rescue Beastie");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Kona, Rescue Beastie"));
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InnocuousRat());
        prepareToCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareToCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or creature");
    }

    @Test
    void exilesArtifactCreatureWithPowerLessThanFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FriendlyTeddy());

        castExorcise(target);

        harness.assertNotOnBattlefield(player2, "Friendly Teddy");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Friendly Teddy"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesEnchantmentCreatureWithPowerLessThanFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EnduringInnocence());

        castExorcise(target);

        harness.assertNotOnBattlefield(player2, "Enduring Innocence");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Enduring Innocence"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FoundFootage());

        castExorcise(target);

        harness.assertNotOnBattlefield(player1, "Found Footage");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Found Footage"));
    }

    @Test
    void usesModifiedPowerWhenChoosingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InnocuousRat());
        target.setPowerModifier(3);

        castExorcise(target);

        harness.assertNotOnBattlefield(player2, "Innocuous Rat");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Innocuous Rat"));
    }

    @Test
    void cannotTargetCreatureWithPowerExactlyThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KonaRescueBeastie());
        target.setPowerModifier(-1);
        prepareToCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    void stillExilesArtifactCreatureWhosePowerDropsBelowFourBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FriendlyTeddy());
        target.setPowerModifier(2);
        prepareToCast();
        harness.castSorcery(player1, 0, target.getId());

        target.setPowerModifier(0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Friendly Teddy");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Friendly Teddy"));
    }

    @Test
    void doesNotExileCreatureWhosePowerDropsBelowFourBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KonaRescueBeastie());
        prepareToCast();
        harness.castSorcery(player1, 0, target.getId());

        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kona, Rescue Beastie");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Exorcise");
        assertThat(gd.stack).isEmpty();
    }

    private void castExorcise(Permanent target) {
        prepareToCast();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void prepareToCast() {
        harness.setHand(player1, List.of(new Exorcise()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
