package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinationGuide.class, MistIntruder.class, OranRiefInvoker.class})
class RuinationGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Other colorless creatures you control get +1/+0")
    void buffsOtherColorlessCreaturesYouControl() {
        harness.addToBattlefield(player1, new RuinationGuide());
        harness.addToBattlefield(player1, new MistIntruder());
        harness.addToBattlefield(player1, new OranRiefInvoker());
        harness.addToBattlefield(player2, new MistIntruder());

        Permanent guide = findPermanent(player1, "Ruination Guide");
        Permanent colorlessCreature = findPermanent(player1, "Mist Intruder");
        Permanent coloredCreature = findPermanent(player1, "Oran-Rief Invoker");
        Permanent opponentColorlessCreature = findPermanent(player2, "Mist Intruder");

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guide)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, colorlessCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colorlessCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentColorlessCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent guide = addAttackingGuide(player1);
        OranRiefInvoker topCard = new OranRiefInvoker();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isNull();
        assertThat(gd.getCardsExiledByPermanent(guide.getId())).isEmpty();
    }

    private Permanent addAttackingGuide(Player player) {
        Permanent guide = addCreatureReady(player, new RuinationGuide());
        guide.setAttacking(true);
        return guide;
    }

    @Test
    @DisplayName("Multiple Guides boost each other and their bonuses stack")
    void multipleGuidesStackAndStopBoostingAfterLeaving() {
        Permanent first = addCreatureReady(player1, new RuinationGuide());
        Permanent second = addCreatureReady(player1, new RuinationGuide());
        Permanent intruder = addCreatureReady(player1, new MistIntruder());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, intruder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, intruder)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, intruder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ingest exiles only the top card, regardless of combat damage amount")
    void ingestExilesExactlyOneCardFromDamagedPlayersLibrary() {
        addAttackingGuide(player1);
        OranRiefInvoker topCard = new OranRiefInvoker();
        MistIntruder nextCard = new MistIntruder();
        RuinationGuide ownTopCard = new RuinationGuide();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setLibrary(player1, List.of(ownTopCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(nextCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTopCard);
    }

    @Test
    @DisplayName("Ingest safely resolves when the damaged player's library is empty")
    void ingestWithEmptyLibrary() {
        addAttackingGuide(player1);
        harness.setLibrary(player2, List.of());
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ingest resolves even after the Guide leaves the battlefield")
    void ingestSurvivesSourceLeavingBattlefield() {
        Permanent guide = addAttackingGuide(player1);
        OranRiefInvoker topCard = new OranRiefInvoker();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        gd.playerBattlefields.get(player1.getId()).remove(guide);
        gd.playerGraveyards.get(player1.getId()).add(guide.getCard());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
