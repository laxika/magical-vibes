package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.d.DromarsAttendant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArtifactPossession.class, AetherSpellbomb.class, DromarsAttendant.class, GrizzlyBears.class,
        IcyManipulator.class, IronMyr.class, Ornithopter.class})
class ArtifactPossessionTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the enchanted artifact deals 2 damage to its controller")
    void tappingEnchantedArtifactDealsDamageToItsController() {
        Permanent artifact = attachAuraTo(player1, player2, new IronMyr());
        artifact.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Activating a non-tap ability of the enchanted artifact deals damage to its controller")
    void controllerActivatingNonTapAbilityDealsDamage() {
        attachAuraTo(player1, player1, new AetherSpellbomb());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Activating a non-tap mana ability of the enchanted artifact deals damage")
    void activatingNonTapManaAbilityDealsDamage() {
        attachAuraTo(player1, player2, new DromarsAttendant());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An opponent activating a non-tap ability deals damage to the enchanted artifact's controller")
    void opponentActivatingNonTapAbilityDealsDamageToArtifactController() {
        attachAuraTo(player1, player2, new AetherSpellbomb());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An ability with a tap cost causes only the tap trigger")
    void tapAbilityDoesNotCauseActivationTrigger() {
        Permanent artifact = attachAuraTo(player1, player2, new IcyManipulator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, target.getId());
        resolveStackFully();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent attachAuraTo(Player auraController, Player artifactController, Card artifactCard) {
        Permanent artifact = harness.addToBattlefieldAndReturn(artifactController, artifactCard);
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new ArtifactPossession());
        aura.setAttachedTo(artifact.getId());
        return artifact;
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
