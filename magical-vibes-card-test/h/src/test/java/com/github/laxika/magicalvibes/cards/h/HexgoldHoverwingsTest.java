package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HexgoldHoverwings.class, BoneSaw.class, GrizzlyBears.class, EnsoulArtifact.class})
class HexgoldHoverwingsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Hexgold Hoverwings creates and equips a 2/2 Rebel token with flying")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new HexgoldHoverwings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent hoverwings = findPermanent(player1, "Hexgold Hoverwings");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
        assertThat(hoverwings.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Hoverwings boosts every equipped creature you control")
    void boostsEveryEquippedCreatureYouControl() {
        Permanent hoverwings = harness.addToBattlefieldAndReturn(player1, new HexgoldHoverwings());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        saw.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(hoverwings.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip moves Hoverwings and its abilities to another creature")
    void equipMovesHoverwings() {
        Permanent hoverwings = harness.addToBattlefieldAndReturn(player1, new HexgoldHoverwings());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(hoverwings.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("For Mirrodin still creates a Rebel when Hoverwings leaves before the trigger resolves")
    void createsRebelAfterEquipmentLeaves() {
        harness.setHand(player1, List.of(new HexgoldHoverwings()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent hoverwings = findPermanent(player1, "Hexgold Hoverwings");
        gd.playerBattlefields.get(player1.getId()).remove(hoverwings);
        gd.playerGraveyards.get(player1.getId()).add(hoverwings.getCard());

        resolveAllTriggers();

        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(countPermanents(player1, "Rebel")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FLYING)).isFalse();
        harness.assertNotOnBattlefield(player1, "Hexgold Hoverwings");
    }

    @Test
    @DisplayName("Re-equipping removes flying and the boost from the now unequipped Rebel")
    void reEquippingUpdatesBothCreatures() {
        harness.setHand(player1, List.of(new HexgoldHoverwings(), new HexgoldHoverwings()));
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent first = findPermanents(player1, "Hexgold Hoverwings").getFirst();
        Permanent oldHost = findPermanents(player1, "Rebel").getFirst();
        Permanent newHost = findPermanents(player1, "Rebel").getLast();
        assertThat(gqs.getEffectivePower(gd, oldHost)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, newHost)).isEqualTo(4);

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(first),
                null, newHost.getId());
        harness.passBothPriorities();

        assertThat(first.getAttachedTo()).isEqualTo(newHost.getId());
        assertThat(gqs.getEffectivePower(gd, oldHost)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, oldHost, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, newHost)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, newHost)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, newHost, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying follows the attachment while the equipped-creature boost follows control")
    void opponentEquippedCreatureGetsFlyingButNotBoost() {
        Permanent hoverwings = harness.addToBattlefieldAndReturn(player1, new HexgoldHoverwings());
        Permanent ownUnequipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingHost = addCreatureReady(player2, new GrizzlyBears());
        hoverwings.setAttachedTo(opposingHost.getId());

        assertThat(gqs.getEffectivePower(gd, ownUnequipped)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingHost)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingHost, Keyword.FLYING)).isTrue();

        hoverwings.setAttachedTo(ownUnequipped.getId());
        assertThat(gqs.getEffectivePower(gd, ownUnequipped)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingHost, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An animated and equipped Hoverwings receives its own equipped-creature boost")
    void animatedEquippedHoverwingsBoostsItself() {
        Permanent hoverwings = harness.addToBattlefieldAndReturn(player1, new HexgoldHoverwings());
        Permanent saw = harness.addToBattlefieldAndReturn(player1, new BoneSaw());
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, hoverwings.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hoverwings)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hoverwings)).isEqualTo(5);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(saw),
                null, hoverwings.getId());
        harness.passBothPriorities();

        assertThat(saw.getAttachedTo()).isEqualTo(hoverwings.getId());
        assertThat(gqs.getEffectivePower(gd, hoverwings)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, hoverwings)).isEqualTo(5);
    }
}
