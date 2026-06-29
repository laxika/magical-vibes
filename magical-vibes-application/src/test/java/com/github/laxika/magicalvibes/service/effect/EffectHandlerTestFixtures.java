package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class EffectHandlerTestFixtures {

    private EffectHandlerTestFixtures() {
    }

    public record TwoPlayerGame(UUID player1Id, UUID player2Id, GameData gameData) {
    }

    public static TwoPlayerGame newTwoPlayerGameData() {
        return newTwoPlayerGameData(true);
    }

    public static TwoPlayerGame newTwoPlayerGameData(boolean withGraveyardsAndHands) {
        UUID player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        GameData gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        if (withGraveyardsAndHands) {
            gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
            gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
            gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
            gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
            gd.playerLifeTotals.put(player1Id, 20);
            gd.playerLifeTotals.put(player2Id, 20);
        }
        return new TwoPlayerGame(player1Id, player2Id, gd);
    }

    public static TwoPlayerGame newTwoPlayerGameDataFull() {
        TwoPlayerGame game = newTwoPlayerGameData(true);
        GameData gd = game.gameData();
        gd.playerDecks.put(game.player1Id(), new ArrayList<>());
        gd.playerDecks.put(game.player2Id(), new ArrayList<>());
        gd.playerManaPools.put(game.player1Id(), new ManaPool());
        gd.playerManaPools.put(game.player2Id(), new ManaPool());
        gd.activePlayerId = game.player1Id();
        return game;
    }

    public static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    public static Card createInstantCard(String name) {
        Card card = createCard(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        return card;
    }

    public static Card createCreatureCard(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    public static Permanent addPermanent(GameData gd, UUID playerId, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    public static StackEntry createEntryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId) {
        StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId, card.getName(), effects);
        entry.setTargetId(targetId);
        return entry;
    }

    public static StackEntry createEntry(Card card, UUID controllerId, List<CardEffect> effects) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId, card.getName(), effects);
    }
}
