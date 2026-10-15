package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.c.CairnWanderer;
import com.github.laxika.magicalvibes.cards.d.DakmorSalvage;
import com.github.laxika.magicalvibes.cards.d.DoggedDetective;
import com.github.laxika.magicalvibes.cards.e.Epochrasite;
import com.github.laxika.magicalvibes.cards.f.FatalAttraction;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.m.MarshalingCry;
import com.github.laxika.magicalvibes.cards.n.Narcomoeba;
import com.github.laxika.magicalvibes.cards.s.SnapcasterMage;
import com.github.laxika.magicalvibes.cards.s.SproutSwarm;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.cards.w.Whetwheel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YixlidJailer.class, AvenInitiate.class, MarshalingCry.class, DakmorSalvage.class,
        DoggedDetective.class, CairnWanderer.class, Epochrasite.class, FatalAttraction.class,
        Lignify.class, Narcomoeba.class, SnapcasterMage.class, SproutSwarm.class,
        Tarmogoyf.class, Whetwheel.class})
class YixlidJailerTest extends BaseCardTest {

    @Test
    void preventsActivatingAbilitiesOfCardsInGraveyards() {
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(new AvenInitiate()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no graveyard activated ability");
    }

    @Test
    void preventsFlashbackFromGraveyards() {
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(new MarshalingCry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preventsDredgeReplacementFromGraveyards() {
        DakmorSalvage salvage = new DakmorSalvage();
        MarshalingCry drawn = new MarshalingCry();
        MarshalingCry remaining = new MarshalingCry();
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(salvage));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn, remaining));

        drawCard(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(salvage);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void preventsGraveyardTriggeredAbilitiesFromFiring() {
        DoggedDetective detective = new DoggedDetective();
        harness.addToBattlefield(player2, new YixlidJailer());
        harness.setGraveyard(player1, List.of(detective));
        harness.setLibrary(player2, List.of(new MarshalingCry(), new MarshalingCry()));

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(detective);
    }

    @Test
    void removesPowerToughnessDefiningAbilitiesOnlyInGraveyard() {
        Tarmogoyf goyf = new Tarmogoyf();
        harness.addToBattlefield(player1, new YixlidJailer());
        harness.setGraveyard(player1, List.of(goyf, new DakmorSalvage(), new MarshalingCry()));

        assertThat(gqs.getEffectiveCardPower(gd, goyf)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, goyf)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new DakmorSalvage(), new MarshalingCry()));
        harness.setHand(player1, List.of(goyf));

        assertThat(gqs.getEffectiveCardPower(gd, goyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, goyf)).isEqualTo(3);
    }

    @Test
    void removingJailersAbilityWithAnAuraRestoresFlashback() {
        Permanent jailer = harness.addToBattlefieldAndReturn(player2, new YixlidJailer());
        MarshalingCry cry = new MarshalingCry();
        harness.setGraveyard(player1, List.of(cry));
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, jailer.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cry);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cry);
    }

    @Test
    void removingJailerFromBattlefieldRestoresFlashback() {
        Permanent jailer = harness.addToBattlefieldAndReturn(player2, new YixlidJailer());
        MarshalingCry cry = new MarshalingCry();
        harness.setGraveyard(player1, List.of(cry));
        harness.setHand(player1, List.of(new FatalAttraction()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, jailer.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Yixlid Jailer");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cry);
    }

    @Test
    void preventsNarcomoebaTriggerWhenMilled() {
        Narcomoeba narcomoeba = new Narcomoeba();
        harness.addToBattlefield(player1, new Whetwheel());
        harness.addToBattlefield(player2, new YixlidJailer());
        harness.setLibrary(player1, List.of(narcomoeba, new MarshalingCry()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(narcomoeba);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Narcomoeba");
    }

    @Test
    void doesNotSuppressDeathTriggersFromBattlefield() {
        Epochrasite epochrasite = new Epochrasite();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, epochrasite);
        harness.addToBattlefield(player2, new YixlidJailer());
        harness.setHand(player1, List.of(new FatalAttraction()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, permanent.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(epochrasite);
        assertThat(gd.exiledCardTimeCounters).containsEntry(epochrasite.getId(), 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(epochrasite);
    }

    @Test
    void preventsCairnWandererFromGainingRemovedGraveyardKeywords() {
        Permanent wanderer = harness.addToBattlefieldAndReturn(player1, new CairnWanderer());
        harness.setGraveyard(player1, List.of(new Narcomoeba()));
        assertThat(gqs.hasKeyword(gd, wanderer, Keyword.FLYING)).isTrue();

        harness.addToBattlefield(player2, new YixlidJailer());

        assertThat(gqs.hasKeyword(gd, wanderer, Keyword.FLYING)).isFalse();
    }

    @Test
    void laterSnapcasterGrantRestoresFlashback() {
        SproutSwarm swarm = new SproutSwarm();
        harness.addToBattlefield(player2, new YixlidJailer());
        harness.setGraveyard(player1, List.of(swarm));
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(swarm.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Saproling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(swarm);
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
