package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.d.DromarsAttendant;
import com.github.laxika.magicalvibes.cards.c.ClayStatue;
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

@CardUsed({ArtifactPossession.class, AetherSpellbomb.class, DromarsAttendant.class, ClayStatue.class,
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
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Activating a non-tap ability of the enchanted artifact deals damage to its controller")
    void controllerActivatingNonTapAbilityDealsDamage() {
        attachAuraTo(player1, player1, new AetherSpellbomb());
        gd.playerDecks.get(player1.getId()).add(new Ornithopter());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Activating a non-tap mana ability of the enchanted artifact deals damage")
    void activatingNonTapManaAbilityDealsDamage() {
        attachAuraTo(player1, player2, new DromarsAttendant());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An opponent activating a non-tap ability deals damage to the enchanted artifact's controller")
    void opponentActivatingNonTapAbilityDealsDamageToArtifactController() {
        attachAuraTo(player1, player2, new AetherSpellbomb());
        gd.playerDecks.get(player2.getId()).add(new Ornithopter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent attachAuraTo(Player auraController, Player artifactController, Card artifactCard) {
        Permanent artifact = harness.addToBattlefieldAndReturn(artifactController, artifactCard);
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new ArtifactPossession());
        aura.setAttachedTo(artifact.getId());
        return artifact;
    }

    @Test
    @DisplayName("Non-tap activation damage follows a control change before resolution")
    void activationDamageUsesControllerAtResolution() {
        Permanent artifact = attachAuraTo(player1, player2, new ClayStatue());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An unrelated artifact's activation does not trigger Artifact Possession")
    void unrelatedActivationDoesNotDealDamage() {
        attachAuraTo(player1, player2, new Ornithopter());
        harness.addToBattlefield(player2, new ClayStatue());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping the enchanted artifact with another artifact's ability deals damage")
    void tappingByAnEffectDealsDamage() {
        Permanent artifact = attachAuraTo(player1, player2, new Ornithopter());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 1, null, artifact.getId());
        resolveAllTriggers();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
