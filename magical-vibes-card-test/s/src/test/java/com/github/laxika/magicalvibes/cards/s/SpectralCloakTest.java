package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralCloak.class, BarbaryApes.class, Boomerang.class})
class SpectralCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted untapped creature has shroud")
    void enchantedUntappedCreatureHasShroud() {
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        attachCloak(apes);

        assertThat(gqs.hasKeyword(gd, apes, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Enchanted tapped creature does not have shroud")
    void enchantedTappedCreatureDoesNotHaveShroud() {
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        apes.tap();
        attachCloak(apes);

        assertThat(gqs.hasKeyword(gd, apes, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature loses and regains shroud as it is tapped and untapped")
    void shroudFollowsEnchantedCreatureTapState() {
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        attachCloak(apes);

        assertThat(gqs.hasKeyword(gd, apes, Keyword.SHROUD)).isTrue();

        apes.tap();
        assertThat(gqs.hasKeyword(gd, apes, Keyword.SHROUD)).isFalse();

        apes.untap();
        assertThat(gqs.hasKeyword(gd, apes, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("An enchanted untapped creature cannot be targeted")
    void enchantedUntappedCreatureCannotBeTargeted() {
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        attachCloak(apes);

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, apes.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("A tapped enchanted creature can be targeted")
    void tappedEnchantedCreatureCanBeTargeted() {
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        apes.tap();
        attachCloak(apes);

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, apes.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting the Cloak attaches it and grants shroud without removing the Aura")
    void castingCloakGrantsShroud() {
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        harness.setHand(player1, List.of(new SpectralCloak()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, apes.getId());
        harness.passBothPriorities();

        Permanent cloak = findPermanent(player1, "Spectral Cloak");
        assertThat(cloak.getAttachedTo()).isEqualTo(apes.getId());
        assertThat(gqs.hasKeyword(gd, apes, Keyword.SHROUD)).isTrue();
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Spectral Cloak");
    }

    @Test
    @DisplayName("The Cloak protects an opponent's creature and no other creature")
    void protectsOnlyEnchantedCreatureRegardlessOfController() {
        Permanent host = addCreatureReady(player2, new BarbaryApes());
        Permanent other = addCreatureReady(player1, new BarbaryApes());
        attachCloak(host);

        assertThat(gqs.hasKeyword(gd, host, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.SHROUD)).isFalse();
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Untapping before resolution makes the creature an illegal target")
    void gainingShroudBeforeResolutionStopsBounce() {
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        apes.tap();
        attachCloak(apes);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, apes.getId());

        apes.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Barbary Apes");
        harness.assertOnBattlefield(player1, "Spectral Cloak");
        harness.assertNotInHand(player1, "Barbary Apes");
        harness.assertInGraveyard(player1, "Boomerang");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Aura can be targeted and its removal ends shroud")
    void bouncingCloakEndsShroud() {
        Permanent apes = addCreatureReady(player1, new BarbaryApes());
        attachCloak(apes);
        Permanent cloak = findPermanent(player1, "Spectral Cloak");
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, cloak.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spectral Cloak");
        harness.assertOnBattlefield(player1, "Barbary Apes");
        assertThat(gqs.hasKeyword(gd, apes, Keyword.SHROUD)).isFalse();
    }

    private void attachCloak(Permanent host) {
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new SpectralCloak());
        cloak.setAttachedTo(host.getId());
    }
}
