package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GristTheHungerTide;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.m.MistyRainforest;
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

@CardUsed({BoneShards.class, GristTheHungerTide.class, OrnithopterOfParadise.class, MistyRainforest.class})
class BoneShardsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and destroys target creature")
    void sacrificesCreatureAndDestroysTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());

        harness.setHand(player1, List.of(new BoneShards()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(sacrifice.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(sacrifice.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Discards a card and destroys target planeswalker")
    void discardsCardAndDestroysPlaneswalker() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 3);
        harness.setHand(player1, List.of(new BoneShards(), new MistyRainforest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithDiscard(player1, 0, planeswalker.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(planeswalker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(planeswalker.getCard().getId()));
        harness.assertInGraveyard(player1, "Misty Rainforest");
    }

    @Test
    @DisplayName("Rejects a non-creature, non-planeswalker target")
    void rejectsLandTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MistyRainforest());

        harness.setHand(player1, List.of(new BoneShards()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID landId = land.getId();
        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, landId, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice or another card to discard")
    void cannotCastWithoutAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());

        harness.setHand(player1, List.of(new BoneShards()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard a card or sacrifice a creature");
    }

    @Test
    @DisplayName("Can sacrifice the targeted creature as the additional cost")
    void canSacrificeTargetedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        harness.setHand(player1, List.of(new BoneShards()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), target.getId());

        harness.assertInGraveyard(player1, "Ornithopter of Paradise");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bone Shards");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature to pay the cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());
        harness.setHand(player1, List.of(new BoneShards()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Ornithopter of Paradise");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent to pay the cost")
    void cannotSacrificeLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MistyRainforest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrnithopterOfParadise());
        harness.setHand(player1, List.of(new BoneShards()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Misty Rainforest");
    }

    @Test
    @DisplayName("Discards a card before Bone Shards in hand and destroys an own creature")
    void discardsEarlierHandCardAndDestroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrnithopterOfParadise());
        harness.setHand(player1, List.of(new MistyRainforest(), new BoneShards()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithDiscard(player1, 1, target.getId(), 0);

        harness.assertInGraveyard(player1, "Misty Rainforest");
        harness.assertOnBattlefield(player1, "Ornithopter of Paradise");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ornithopter of Paradise");
        harness.assertInGraveyard(player1, "Ornithopter of Paradise");
    }
    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GristTheHungerTide());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        return perm;
    }
}
