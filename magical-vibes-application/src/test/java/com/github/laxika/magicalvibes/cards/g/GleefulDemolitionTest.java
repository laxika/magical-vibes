package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BasilicaSkullbomb;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GleefulDemolition.class, Spellbook.class, Forest.class,
        DarksteelCitadel.class, BasilicaSkullbomb.class})
class GleefulDemolitionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's artifact without creating tokens")
    void destroysOpponentsArtifactWithoutTokens() {
        harness.addToBattlefield(player2, new Spellbook());
        UUID targetId = harness.getPermanentId(player2, "Spellbook");

        cast(targetId);

        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
        assertThat(countPermanents(player2, "Phyrexian Goblin")).isZero();
    }

    @Test
    @DisplayName("Destroys your artifact and creates three Phyrexian Goblin tokens")
    void destroysOwnArtifactAndCreatesTokens() {
        harness.addToBattlefield(player1, new Spellbook());
        UUID targetId = harness.getPermanentId(player1, "Spellbook");

        cast(targetId);

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(3);
        assertThat(findPermanents(player1, "Phyrexian Goblin"))
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(
                            CardSubtype.PHYREXIAN, CardSubtype.GOBLIN);
                });
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GleefulDemolition()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Creates tokens even when your artifact is indestructible")
    void createsTokensFromOwnIndestructibleArtifact() {
        harness.addToBattlefield(player1, new DarksteelCitadel());

        cast(harness.getPermanentId(player1, "Darksteel Citadel"));

        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        harness.assertNotInGraveyard(player1, "Darksteel Citadel");
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(3);
        assertThat(countPermanents(player2, "Phyrexian Goblin")).isZero();
    }

    @Test
    @DisplayName("Creates no tokens when the target is sacrificed before resolution")
    void createsNoTokensWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new BasilicaSkullbomb());
        UUID targetId = harness.getPermanentId(player1, "Basilica Skullbomb");
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GleefulDemolition()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, targetId);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Basilica Skullbomb");
        harness.assertInGraveyard(player1, "Gleeful Demolition");
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
        assertThat(countPermanents(player2, "Phyrexian Goblin")).isZero();
    }

    private void cast(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GleefulDemolition()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
