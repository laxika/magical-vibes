package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Wear // Tear is one card whose two halves (and their fusion) are the three modes of a single
 * modal instant, each paying its own total cost.
 */
class WearTearTest extends BaseCardTest {

    private static final int WEAR = 0;
    private static final int TEAR = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Wear destroys the targeted artifact")
    void wearDestroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, WEAR, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Wear cannot be cast with white mana alone")
    void wearCannotBeCastWithWhiteManaAlone() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, WEAR, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Tear destroys the targeted enchantment")
    void tearDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, TEAR, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Tear is castable off white mana alone — the mode's {W} replaces the printed Wear cost")
    void tearIsPaidWithItsOwnCost() {
        harness.addToBattlefield(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, TEAR, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Fuse resolves Wear then Tear on independent targets")
    void fuseUsesIndependentTargets() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castModalInstant(player1, 0, FUSE, List.of(artifactId, enchantmentId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Fuse may put both halves on the same artifact enchantment")
    void fuseAllowsSharedTarget() {
        Permanent both = addArtifactEnchantment(player2, "Test Relic");

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, FUSE, List.of(both.getId(), both.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Test Relic");
        harness.assertInGraveyard(player2, "Test Relic");
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        Permanent both = addArtifactEnchantment(player2, "Test Relic");

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bothId = both.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE, List.of(bothId, bothId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wear cannot target a non-artifact")
    void wearCannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, WEAR, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tear cannot target an artifact")
    void tearCannotTargetArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new WearTear()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, TEAR, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addArtifactEnchantment(Player player, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
