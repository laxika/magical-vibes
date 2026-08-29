package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.Godsire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuildpactParagon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KitchenFinks;
import com.github.laxika.magicalvibes.cards.o.OonaQueenOfTheFae;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        Godsire.class,
        GrizzlyBears.class,
        GuildpactParagon.class,
        InvasionOfRavnica.class,
        Island.class,
        KitchenFinks.class,
        OonaQueenOfTheFae.class,
        Plains.class,
        Shock.class
})
class InvasionOfRavnicaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters targeting an opponent's nonland permanent that is not exactly two colors")
    void etbExilesNonTwoColorPermanent() {
        Permanent exactlyTwoColors = harness.addToBattlefieldAndReturn(player2, new OonaQueenOfTheFae());
        Permanent threeColors = harness.addToBattlefieldAndReturn(player2, new Godsire());
        Card invasion = new InvasionOfRavnica();

        harness.setHand(player1, List.of(invasion));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        var targets = harness.getValidTargetService().computeValidTargetsForSpell(
                gd, invasion, player1.getId(), null);
        assertThat(targets.validPermanentIds()).contains(threeColors.getId())
                .doesNotContain(exactlyTwoColors.getId());

        gs.playCard(gd, player1, 0, 0, threeColors.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(exactlyTwoColors)
                .noneMatch(permanent -> permanent.getId().equals(threeColors.getId()));
    }

    @Test
    @DisplayName("Defeat exiles the Siege and casts Guildpact Paragon transformed")
    void defeatCastsBackFace() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Godsire());
        castInvasion(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent battle = findPermanentByName(player1, "Invasion of Ravnica");
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent paragon = findPermanentByName(player1, "Guildpact Paragon");
        assertThat(paragon.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Guildpact Paragon triggers only for exactly two-color spells and cards")
    void backFaceTriggersForExactlyTwoColors() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new GuildpactParagon());
        paragon.setTransformed(true);
        Card exactlyTwoColors = new OonaQueenOfTheFae();
        Card threeColors = new Godsire();
        setupTopCards(List.of(exactlyTwoColors, threeColors, new GrizzlyBears(),
                new Island(), new Shock(), new Plains()));

        harness.setHand(player1, List.of(new KitchenFinks()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(exactlyTwoColors.getId())
                .doesNotContain(threeColors.getId());

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(exactlyTwoColors.getId())));
        assertThat(gd.playerHands.get(player1.getId())).contains(exactlyTwoColors);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castInvasion(UUID targetId) {
        harness.setHand(player1, List.of(new InvasionOfRavnica()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }

    private void setupTopCards(List<Card> cards) {
        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player1.getId()).clear();
        gameData.playerDecks.get(player1.getId()).addAll(cards);
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
