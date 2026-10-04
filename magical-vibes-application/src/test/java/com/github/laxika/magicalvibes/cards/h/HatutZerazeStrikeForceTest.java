package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.r.RaffCapashenShipsMage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HatutZerazeStrikeForce.class, EdgarMarkov.class, FountainOfYouth.class,
        AngelicChorus.class, GrizzlyBears.class, RaffCapashenShipsMage.class})
class HatutZerazeStrikeForceTest extends BaseCardTest {

    @Test
    void destroysAnArtifactOrEnchantmentWhenItEnters() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        cast(List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void mayEnterWithoutChoosingATarget() {
        cast(List.of());

        harness.assertOnBattlefield(player1, "Hatut Zeraze Strike Force");
    }

    @Test
    void destroysAnEnchantmentWhenItEnters() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        cast(List.of(enchantment.getId()));

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void mayDeclineToDestroyAnAvailableArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        cast(List.of());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
            resolveAllTriggers();
        }

        harness.assertOnBattlefield(player1, "Hatut Zeraze Strike Force");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsCommanderCastsWhenTheCopyAbilityResolves() {
        Card commander = new RaffCapashenShipsMage();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));

        harness.castFromHand(player1, new HatutZerazeStrikeForce(), "{3}{W}");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        gs.castCommander(gd, player1, commander.getId(),
                () -> harness.castCreature(player1, 0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Hatut Zeraze Strike Force")))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Hatut Zeraze Strike Force"))
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    void copiesItselfForEachCommanderCast() {
        Card commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.commanderTaxByCardId.put(commander.getId(), 4);

        cast(List.of());

        List<Permanent> copies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Hatut Zeraze Strike Force"))
                .toList();
        assertThat(copies).hasSize(3);
        assertThat(copies.stream().filter(permanent -> permanent.getCard().isToken())).hasSize(2);
    }

    @Test
    void cannotTargetACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    private void cast(List<java.util.UUID> targetIds) {
        if (targetIds.isEmpty()) {
            harness.castFromHand(player1, new HatutZerazeStrikeForce(), "{3}{W}");
            resolveAllTriggers();
            return;
        }
        harness.setHand(player1, List.of(new HatutZerazeStrikeForce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }
}
