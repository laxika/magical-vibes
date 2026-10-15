package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PortentOfBetrayal;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.m.MagmaJet;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellheartChimera.class, LightningStrike.class, MagmaJet.class, PortentOfBetrayal.class, Plains.class, BronzeSable.class})
class SpellheartChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Power is 0 with an empty graveyard; toughness stays 3")
    void powerZeroWithEmptyGraveyard() {
        Permanent chimera = addChimeraReady(player1);

        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, chimera)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power equals the number of instant and sorcery cards in your graveyard; toughness stays 3")
    void powerEqualsInstantsAndSorceriesInOwnGraveyard() {
        Permanent chimera = addChimeraReady(player1);
        harness.setGraveyard(player1, List.of(new LightningStrike(), new MagmaJet(), new PortentOfBetrayal()));

        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chimera)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only instant and sorcery cards count, not other card types")
    void onlyCountsInstantsAndSorceries() {
        Permanent chimera = addChimeraReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new LightningStrike());
        graveyard.add(new PortentOfBetrayal());
        graveyard.add(new Plains());
        graveyard.add(new BronzeSable());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chimera)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count instant and sorcery cards in an opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent chimera = addChimeraReady(player1);
        harness.setGraveyard(player1, List.of(new LightningStrike()));
        harness.setGraveyard(player2, List.of(new MagmaJet(), new PortentOfBetrayal()));

        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chimera)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates as instant and sorcery cards enter the graveyard")
    void powerUpdatesWhenSpellsAdded() {
        Permanent chimera = addChimeraReady(player1);
        harness.setGraveyard(player1, List.of(new LightningStrike()));

        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new PortentOfBetrayal());

        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chimera)).isEqualTo(3);
    }

    private Permanent addChimeraReady(Player player) {
        return addCreatureReady(player, new SpellheartChimera());
    }

    @Test
    void powerDecreasesWhenSpellsLeaveGraveyard() {
        Permanent chimera = addChimeraReady(player1);
        harness.setGraveyard(player1, List.of(new LightningStrike(), new PortentOfBetrayal()));
        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new PortentOfBetrayal()));
        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(1);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, chimera)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, chimera)).isEqualTo(3);
    }

    @Test
    void powerIsDefinedInHandAndGraveyard() {
        SpellheartChimera chimera = new SpellheartChimera();
        harness.setHand(player1, List.of(chimera));
        harness.setGraveyard(player1, List.of(new LightningStrike(), new PortentOfBetrayal()));
        harness.setGraveyard(player2, List.of(new MagmaJet()));

        assertThat(gqs.getEffectiveCardPower(gd, chimera)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, chimera)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(chimera, new LightningStrike(), new PortentOfBetrayal()));

        assertThat(gqs.getEffectiveCardPower(gd, chimera)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, chimera)).isEqualTo(3);
    }

    @Test
    void powerUsesNewControllersGraveyardAfterControlChanges() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent chimera = addChimeraReady(player2);
        harness.setGraveyard(player2, List.of(new MagmaJet()));
        harness.setGraveyard(player1, List.of(new LightningStrike(), new MagmaJet()));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new PortentOfBetrayal()));
        harness.addMana(player1, ManaColor.RED, 4);
        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(1);

        harness.castAndResolveSorcery(player1, 0, chimera.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chimera);
        assertThat(gqs.getEffectivePower(gd, chimera)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chimera)).isEqualTo(3);
    }
}
