package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.b.BattleCryGoblin;
import com.github.laxika.magicalvibes.cards.b.BeetlebackChief;
import com.github.laxika.magicalvibes.cards.b.BrashTaunter;
import com.github.laxika.magicalvibes.cards.e.EmberHauler;
import com.github.laxika.magicalvibes.cards.f.FanaticalFirebrand;
import com.github.laxika.magicalvibes.cards.g.GoblinArsonist;
import com.github.laxika.magicalvibes.cards.g.GoblinChieftain;
import com.github.laxika.magicalvibes.cards.g.GoblinInstigator;
import com.github.laxika.magicalvibes.cards.g.GoblinTrashmaster;
import com.github.laxika.magicalvibes.cards.g.GoblinWarchief;
import com.github.laxika.magicalvibes.cards.l.LegionWarboss;
import com.github.laxika.magicalvibes.cards.r.RelicRobber;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureGoblinInfluxArraySpellbookEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Goblin Influx Array's random Goblin spellbook. */
@Component
@RequiredArgsConstructor
public class ConjureGoblinInfluxArraySpellbookEffectHandler implements NormalEffectHandlerBean {

    private static final List<String> SPELLBOOK_NAMES = List.of(
            "Goblin Warchief", "Goblin Chieftain", "Skirk Prospector", "Brash Taunter",
            "Wily Goblin", "Goblin Trashmaster", "Ember Hauler", "Relic Robber",
            "Fanatical Firebrand", "Goblin Arsonist", "Reckless Ringleader", "Battle Cry Goblin",
            "Beetleback Chief", "Goblin Instigator", "Legion Warboss");

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureGoblinInfluxArraySpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        if (hand == null) {
            return;
        }

        String cardName = SPELLBOOK_NAMES.get(ThreadLocalRandom.current().nextInt(SPELLBOOK_NAMES.size()));
        hand.add(createSpellbookCard(cardName, entry.getControllerId()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a random card from its spellbook into their hand."));
    }

    private Card createSpellbookCard(String name, UUID ownerId) {
        return switch (name) {
            case "Goblin Warchief" -> creature(new GoblinWarchief(), ownerId, name, "{1}{R}{R}", 2, 2,
                    List.of(CardSubtype.GOBLIN, CardSubtype.WARRIOR), Set.of(),
                    "Goblin spells you cast cost {1} less to cast.\nGoblins you control have haste.");
            case "Goblin Chieftain" -> creature(new GoblinChieftain(), ownerId, name, "{1}{R}{R}", 2, 2,
                    List.of(CardSubtype.GOBLIN), Set.of(Keyword.HASTE),
                    "Haste\nOther Goblin creatures you control get +1/+1 and have haste.");
            case "Skirk Prospector" -> creature(new SkirkProspector(), ownerId, name, "{R}", 1, 1,
                    List.of(CardSubtype.GOBLIN), Set.of(), "Sacrifice a Goblin: Add {R}.");
            case "Brash Taunter" -> creature(new BrashTaunter(), ownerId, name, "{4}{R}", 1, 1,
                    List.of(CardSubtype.GOBLIN), Set.of(Keyword.INDESTRUCTIBLE),
                    "Indestructible\nWhenever this creature is dealt damage, it deals that much damage to target opponent.\n{2}{R}, {T}: This creature fights another target creature.");
            case "Wily Goblin" -> creature(new WilyGoblin(), ownerId, name, "{R}{R}", 1, 1,
                    List.of(CardSubtype.GOBLIN, CardSubtype.PIRATE), Set.of(),
                    "When this creature enters, create a Treasure token. (It's an artifact with \"{T}, Sacrifice this token: Add one mana of any color.\")");
            case "Goblin Trashmaster" -> creature(new GoblinTrashmaster(), ownerId, name, "{2}{R}{R}", 3, 3,
                    List.of(CardSubtype.GOBLIN, CardSubtype.WARRIOR), Set.of(),
                    "Other Goblins you control get +1/+1.\nSacrifice a Goblin: Destroy target artifact.");
            case "Ember Hauler" -> creature(new EmberHauler(), ownerId, name, "{R}{R}", 2, 2,
                    List.of(CardSubtype.GOBLIN), Set.of(), "{1}, Sacrifice this creature: It deals 2 damage to any target.");
            case "Relic Robber" -> creature(new RelicRobber(), ownerId, name, "{2}{R}", 2, 2,
                    List.of(CardSubtype.GOBLIN, CardSubtype.ROGUE), Set.of(Keyword.HASTE),
                    "Haste\nWhenever this creature deals combat damage to a player, that player creates a 0/1 colorless Goblin Construct artifact creature token with \"This token can't block\" and \"At the beginning of your upkeep, this token deals 1 damage to you.\"");
            case "Fanatical Firebrand" -> creature(new FanaticalFirebrand(), ownerId, name, "{R}", 1, 1,
                    List.of(CardSubtype.GOBLIN, CardSubtype.PIRATE), Set.of(Keyword.HASTE),
                    "Haste\n{T}, Sacrifice this creature: It deals 1 damage to any target.");
            case "Goblin Arsonist" -> creature(new GoblinArsonist(), ownerId, name, "{R}", 1, 1,
                    List.of(CardSubtype.GOBLIN, CardSubtype.SHAMAN), Set.of(),
                    "When this creature dies, you may have it deal 1 damage to any target.");
            case "Reckless Ringleader" -> creature(new Card(), ownerId, name, "{R}", 1, 1,
                    List.of(CardSubtype.GOBLIN, CardSubtype.ROGUE), Set.of(Keyword.HASTE),
                    "Haste\nWhen Reckless Ringleader enters the battlefield, choose a creature card in your hand. It perpetually gains haste.");
            case "Battle Cry Goblin" -> creature(new BattleCryGoblin(), ownerId, name, "{1}{R}", 2, 2,
                    List.of(CardSubtype.GOBLIN), Set.of(),
                    "{1}{R}: Goblins you control get +1/+0 and gain haste until end of turn.\nPack tactics - Whenever this creature attacks, if you attacked with creatures with total power 6 or greater this combat, create a 1/1 red Goblin creature token that's tapped and attacking.");
            case "Beetleback Chief" -> creature(new BeetlebackChief(), ownerId, name, "{2}{R}{R}", 2, 2,
                    List.of(CardSubtype.GOBLIN, CardSubtype.WARRIOR), Set.of(),
                    "When this creature enters, create two 1/1 red Goblin creature tokens.");
            case "Goblin Instigator" -> creature(new GoblinInstigator(), ownerId, name, "{1}{R}", 1, 1,
                    List.of(CardSubtype.GOBLIN, CardSubtype.ROGUE), Set.of(),
                    "When this creature enters, create a 1/1 red Goblin creature token.");
            case "Legion Warboss" -> creature(new LegionWarboss(), ownerId, name, "{2}{R}", 2, 2,
                    List.of(CardSubtype.GOBLIN, CardSubtype.SOLDIER), Set.of(),
                    "Mentor\nAt the beginning of combat on your turn, create a 1/1 red Goblin creature token with haste. That token attacks this combat if able.");
            default -> throw new IllegalStateException("Unknown Goblin Influx Array spellbook card: " + name);
        };
    }

    private Card creature(Card card, UUID ownerId, String name, String manaCost, int power, int toughness,
                          List<CardSubtype> subtypes, Set<Keyword> keywords, String cardText) {
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(CardColor.RED);
        card.setColors(List.of(CardColor.RED));
        card.setColorIdentity(List.of(CardColor.RED));
        card.setSubtypes(subtypes);
        card.setCardText(cardText);
        card.setPower(power);
        card.setToughness(toughness);
        card.setKeywords(keywords);
        card.setOwnerId(ownerId);
        card.setToken(true);
        card.setTokenCard(true);
        card.freeze();
        return card;
    }
}
