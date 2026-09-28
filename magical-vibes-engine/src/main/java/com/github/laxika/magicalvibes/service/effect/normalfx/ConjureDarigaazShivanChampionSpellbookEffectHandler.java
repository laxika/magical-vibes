package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.b.BlackDragon;
import com.github.laxika.magicalvibes.cards.b.BoneDragon;
import com.github.laxika.magicalvibes.cards.d.DemandingDragon;
import com.github.laxika.magicalvibes.cards.i.ImmersturmPredator;
import com.github.laxika.magicalvibes.cards.l.LeylineTyrant;
import com.github.laxika.magicalvibes.cards.m.ManaformHellkite;
import com.github.laxika.magicalvibes.cards.m.MoonveilRegent;
import com.github.laxika.magicalvibes.cards.r.RedDragon;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.SkarrganHellkite;
import com.github.laxika.magicalvibes.cards.s.SkyshipStalker;
import com.github.laxika.magicalvibes.cards.t.TerrorOfThePeaks;
import com.github.laxika.magicalvibes.cards.t.ThunderbreakRegent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDarigaazShivanChampionSpellbookEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Darigaaz, Shivan Champion's random Dragon spellbook. */
@Component
@RequiredArgsConstructor
public class ConjureDarigaazShivanChampionSpellbookEffectHandler implements NormalEffectHandlerBean {

    private static final List<String> SPELLBOOK_NAMES = List.of(
            "Shivan Dragon", "Moonveil Regent", "Terror of the Peaks", "Leyline Tyrant",
            "Immersturm Predator", "Manaform Hellkite", "Bone Dragon", "Demanding Dragon",
            "Skarrgan Hellkite", "Thunderbreak Regent", "Black Dragon", "Skyship Stalker",
            "Red Dragon");

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDarigaazShivanChampionSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID ownerId = entry.getControllerId();
        String cardName = SPELLBOOK_NAMES.get(ThreadLocalRandom.current().nextInt(SPELLBOOK_NAMES.size()));
        Card card = createSpellbookCard(cardName, ownerId);

        gameData.addToExile(ownerId, card, null, true);
        gameData.exiledCardEggCounters.put(card.getId(), 3);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures a random card from its spellbook into exile face down with three egg counters."));
    }

    private Card createSpellbookCard(String name, UUID ownerId) {
        return switch (name) {
            case "Shivan Dragon" -> creature(new ShivanDragon(), ownerId, name, "{4}{R}{R}", 5, 5,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\n{R}: This creature gets +1/+0 until end of turn.");
            case "Moonveil Regent" -> creature(new MoonveilRegent(), ownerId, name, "{3}{R}", 4, 4,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\nWhenever you cast a spell, you may discard your hand. If you do, draw a card for each of that spell's colors.\nWhen this creature dies, it deals X damage to any target, where X is the number of colors among permanents you control.");
            case "Terror of the Peaks" -> creature(new TerrorOfThePeaks(), ownerId, name, "{3}{R}{R}", 5, 4,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\nSpells your opponents cast that target this creature cost an additional 3 life to cast.\nWhenever another creature you control enters, this creature deals damage equal to that creature's power to any target.");
            case "Leyline Tyrant" -> creature(new LeylineTyrant(), ownerId, name, "{2}{R}{R}", 4, 4,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\nYou don't lose unspent red mana as steps and phases end.\nWhen this creature dies, you may pay any amount of {R}. When you do, it deals that much damage to any target.");
            case "Immersturm Predator" -> creature(new ImmersturmPredator(), ownerId, name, "{2}{B}{R}", 3, 3,
                    CardColor.BLACK, List.of(CardColor.BLACK, CardColor.RED),
                    List.of(CardSubtype.VAMPIRE, CardSubtype.DRAGON), Set.of(Keyword.FLYING),
                    "Flying\nWhenever this creature becomes tapped, exile up to one target card from a graveyard and put a +1/+1 counter on this creature.\nSacrifice another creature: This creature gains indestructible until end of turn. Tap it.");
            case "Manaform Hellkite" -> creature(new ManaformHellkite(), ownerId, name, "{2}{R}{R}", 4, 4,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\nWhenever you cast a noncreature spell, create an X/X red Dragon Illusion creature token with flying and haste, where X is the amount of mana spent to cast that spell. Exile that token at the beginning of the next end step.");
            case "Bone Dragon" -> creature(new BoneDragon(), ownerId, name, "{3}{B}{B}", 5, 4,
                    CardColor.BLACK, List.of(CardColor.BLACK), List.of(CardSubtype.DRAGON, CardSubtype.SKELETON),
                    Set.of(Keyword.FLYING), "Flying\n{3}{B}{B}, Exile seven other cards from your graveyard: Return this card from your graveyard to the battlefield tapped.");
            case "Demanding Dragon" -> creature(new DemandingDragon(), ownerId, name, "{3}{R}{R}", 5, 5,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\nWhen this creature enters, it deals 5 damage to target opponent unless that player sacrifices a creature of their choice.");
            case "Skarrgan Hellkite" -> creature(new SkarrganHellkite(), ownerId, name, "{3}{R}{R}", 4, 4,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING, Keyword.RIOT), "Riot (This creature enters with your choice of a +1/+1 counter or haste.)\nFlying\n{3}{R}: This creature deals 2 damage divided as you choose among one or two targets. Activate only if this creature has a +1/+1 counter on it.");
            case "Thunderbreak Regent" -> creature(new ThunderbreakRegent(), ownerId, name, "{2}{R}{R}", 4, 4,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\nWhenever a Dragon you control becomes the target of a spell or ability an opponent controls, this creature deals 3 damage to that player.");
            case "Black Dragon" -> creature(new BlackDragon(), ownerId, name, "{5}{B}{B}", 4, 4,
                    CardColor.BLACK, List.of(CardColor.BLACK), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\nAcid Breath — When this creature enters, target creature an opponent controls gets -3/-3 until end of turn.");
            case "Skyship Stalker" -> creature(new SkyshipStalker(), ownerId, name, "{2}{R}{R}", 3, 3,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.CAT, CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\n{R}: This creature gets +1/+0 until end of turn.\n{R}: This creature gains first strike until end of turn.\n{R}: This creature gains haste until end of turn.");
            case "Red Dragon" -> creature(new RedDragon(), ownerId, name, "{4}{R}{R}", 4, 4,
                    CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                    Set.of(Keyword.FLYING), "Flying\nFire Breath — When this creature enters, it deals 4 damage to each opponent.");
            default -> throw new IllegalStateException("Unknown Darigaaz, Shivan Champion spellbook card: " + name);
        };
    }

    private Card creature(Card card, UUID ownerId, String name, String manaCost, int power, int toughness,
                          CardColor color, List<CardColor> colors, List<CardSubtype> subtypes,
                          Set<Keyword> keywords, String cardText) {
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setColors(colors);
        card.setColorIdentity(colors);
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
