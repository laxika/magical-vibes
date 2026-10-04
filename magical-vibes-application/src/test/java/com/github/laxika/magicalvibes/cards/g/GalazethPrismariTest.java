package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArcaneSubtraction;
import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalazethPrismari.class, CampusGuide.class, EnvironmentalSciences.class, ArcaneSubtraction.class})
class GalazethPrismariTest extends BaseCardTest {

    @Test
    void entersAndCreatesTreasure() {
        castGalazeth();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void artifactsGainRestrictedAnyColorManaAbility() {
        castGalazeth();
        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);

        harness.activateAbility(player1, treasureIndex, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED))
                .isEqualTo(1);
        assertThat(treasure.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Treasure")).isSameAs(treasure);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaPaysForAnInstant() {
        castGalazeth();
        addTreasureMana("BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new ArcaneSubtraction()));

        harness.castInstant(player1, 0, findPermanent(player1, "Galazeth Prismari").getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    void restrictedManaPaysGenericSorceryCost() {
        castGalazeth();
        addTreasureMana("RED");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EnvironmentalSciences()));

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Environmental Sciences");
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED))
                .isZero();
    }

    @Test
    void restrictedManaCannotPayForAnArtifactCreature() {
        castGalazeth();
        addTreasureMana("RED");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new CampusGuide()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Campus Guide");
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    void artifactCreaturesMustOvercomeSummoningSicknessToTapForMana() {
        castGalazeth();
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        guide.setSummoningSick(true);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(guide);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(guide.isTapped()).isFalse();

        guide.setSummoningSick(false);
        harness.activateAbility(player1, index, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(guide.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    void opponentsArtifactsDoNotGainTheAbility() {
        castGalazeth();
        Permanent guide = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        guide.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid ability index");
        assertThat(guide.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void treasureKeepsItsOriginalSacrificeAbility() {
        castGalazeth();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));

        harness.activateAbility(player1, index, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isZero();
    }

    @Test
    void artifactsLoseTheGrantedAbilityWhenGalazethLeaves() {
        castGalazeth();
        Permanent guide = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        guide.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Galazeth Prismari"));
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(guide);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid ability index");
        assertThat(guide.isTapped()).isFalse();
    }

    private void addTreasureMana(String color) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, index, 1, null, null);
        harness.handleListChoice(player1, color);
    }

    private void castGalazeth() {
        harness.castFromHand(player1, new GalazethPrismari(), "{2}{U}{R}");
        resolveAllTriggers();
    }
}
