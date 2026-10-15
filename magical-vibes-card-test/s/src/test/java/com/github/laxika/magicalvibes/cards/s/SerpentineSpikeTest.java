package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentineSpike.class, AirElemental.class, AltarsReap.class, GiantSpider.class, GrizzlyBears.class})
class SerpentineSpikeTest extends BaseCardTest {

    private void addManaAndCast(List<UUID> targets) {
        harness.setHand(player1, List.of(new SerpentineSpike()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, targets);
    }

    @Test
    @DisplayName("Deals 2, 3, and 4 damage to the three creature targets in order")
    void dealsOrderedDamageToCreatureTargets() {
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());

        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player2.getId());
        addManaAndCast(List.of(
                battlefield.get(0).getId(),
                battlefield.get(1).getId(),
                battlefield.get(2).getId()));

        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(battlefield.get(0).getMarkedDamage()).isEqualTo(2);
        assertThat(battlefield.get(1).getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethally damaged targets are exiled instead of put into the graveyard")
    void lethallyDamagedTargetsAreExiled() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player2.getId());
        addManaAndCast(List.of(
                battlefield.get(0).getId(),
                battlefield.get(1).getId(),
                battlefield.get(2).getId()));

        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Grizzly Bears");
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Requires three distinct creature targets")
    void requiresThreeDistinctCreatureTargets() {
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new SerpentineSpike()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID firstTarget = battlefield.get(0).getId();
        UUID secondTarget = battlefield.get(1).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstTarget, secondTarget, firstTarget)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Remaining targets retain their damage amounts when the middle target is sacrificed in response")
    void resolvesWithAnIllegalMiddleTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new SerpentineSpike(), new AltarsReap()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GiantSpider()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        harness.castInstantWithSacrifice(player1, 0, null, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Air Elemental");
    }

    @Test
    @DisplayName("A creature that survives the damage is exiled if sacrificed later that turn")
    void exilesSurvivorSacrificedLaterThatTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addManaAndCast(List.of(first.getId(), second.getId(), third.getId()));
        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, null, first.getId());

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Giant Spider");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Giant Spider"));
    }
}
