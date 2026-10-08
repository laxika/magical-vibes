package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.cards.r.RakdosKeyrune;
import com.github.laxika.magicalvibes.cards.r.RootbornDefenses;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunderingGrowth.class, RakdosKeyrune.class, SphereOfSafety.class, AxebaneStag.class,
        CallOfTheConclave.class, RootbornDefenses.class, Card.class})
class SunderingGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted artifact and populates a creature token")
    void destroysArtifactAndPopulates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakdosKeyrune());
        harness.addToBattlefield(player1, soldierToken());
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Rakdos Keyrune");
        harness.assertInGraveyard(player2, "Rakdos Keyrune");
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroys the targeted enchantment and populates a creature token")
    void destroysEnchantmentAndPopulates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SphereOfSafety());
        harness.addToBattlefield(player1, soldierToken());
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Sphere of Safety");
        harness.assertInGraveyard(player2, "Sphere of Safety");
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither an artifact nor an enchantment")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AxebaneStag());
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = gd.playerBattlefields.get(player2.getId()).getFirst().getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys the target even without a creature token to populate")
    void destroysWithoutCreatureToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakdosKeyrune());
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Rakdos Keyrune");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An illegal artifact target prevents populate")
    void illegalTargetPreventsPopulate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakdosKeyrune());
        createCentaur();
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new SunderingGrowth()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Rakdos Keyrune");
        harness.passBothPriorities();

        assertThat(countOf(player1, "Centaur")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Sundering Growth");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Populate cannot copy an opponent's token or a nontoken creature")
    void ignoresOpposingTokensAndNontokenCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakdosKeyrune());
        harness.addToBattlefield(player2, soldierToken());
        harness.addToBattlefield(player1, new AxebaneStag());
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Rakdos Keyrune");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Populate chooses among own creature tokens after destroying the target")
    void choosesTokenDuringResolution() {
        createCentaur();
        Permanent centaur = gd.playerBattlefields.get(player1.getId()).getFirst();
        centaur.tap();
        centaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, soldierToken());
        harness.addToBattlefield(player2, soldierToken());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakdosKeyrune());
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Rakdos Keyrune");
        assertThat(countOf(player1, "Centaur")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, centaur.getId());

        assertThat(countOf(player1, "Centaur")).isEqualTo(2);
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(1);
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Centaur".equals(p.getCard().getName()) && !p.getId().equals(centaur.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getPlusOnePlusOneCounters()).isZero();
        assertThat(centaur.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(centaur.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Sundering Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May destroy an artifact you control and still populate")
    void destroysOwnArtifactAndPopulates() {
        createCentaur();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RakdosKeyrune());
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Rakdos Keyrune");
        assertThat(countOf(player1, "Centaur")).isEqualTo(2);
    }

    @Test
    @DisplayName("An indestructible artifact creature remains a legal target and does not prevent populate")
    void indestructibleTargetStillPopulates() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RakdosKeyrune());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new RootbornDefenses()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0);
        createCentaur();
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Rakdos Keyrune");
        harness.assertNotInGraveyard(player1, "Rakdos Keyrune");
        assertThat(countOf(player1, "Centaur")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Sundering Growth");
    }

    @Test
    @DisplayName("Cannot cast Sundering Growth without an artifact or enchantment target")
    void cannotCastWithoutTarget() {
        createCentaur();
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countOf(player1, "Centaur")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void createCentaur() {
        harness.setHand(player1, List.of(new CallOfTheConclave()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .count();
    }

    private static Card soldierToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
