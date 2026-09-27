package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.b.BenalishMarshal;
import com.github.laxika.magicalvibes.cards.b.BladeHistorian;
import com.github.laxika.magicalvibes.cards.c.CaptivatingCrew;
import com.github.laxika.magicalvibes.cards.d.DuelcraftTrainer;
import com.github.laxika.magicalvibes.cards.f.FalconerAdept;
import com.github.laxika.magicalvibes.cards.m.ManaformHellkite;
import com.github.laxika.magicalvibes.cards.m.MoonveilRegent;
import com.github.laxika.magicalvibes.cards.o.OgreBattledriver;
import com.github.laxika.magicalvibes.cards.r.ResplendentAngel;
import com.github.laxika.magicalvibes.cards.s.SeraphOfDawn;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SerraParagon;
import com.github.laxika.magicalvibes.cards.s.SkyshipStalker;
import com.github.laxika.magicalvibes.cards.s.StarCrownedStag;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftProteanWarEngineSpellbookEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DraftProteanWarEngineSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftProteanWarEngineSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() != null) {
            return;
        }

        List<Card> spellbook = createSpellbook(entry.getControllerId());
        Collections.shuffle(spellbook, ThreadLocalRandom.current());
        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData,
                new com.github.laxika.magicalvibes.model.PendingInteraction.ProteanWarEngineSpellbookDraftChoice(
                        entry.getControllerId(), entry.getSourcePermanentId(),
                        List.copyOf(spellbook.subList(0, 3))));
    }

    private List<Card> createSpellbook(UUID ownerId) {
        return new ArrayList<>(List.of(
                creature(new SerraAngel(), ownerId, "Serra Angel", "{3}{W}{W}", 4, 4,
                        CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.ANGEL),
                        Set.of(Keyword.FLYING, Keyword.VIGILANCE), "Flying, vigilance"),
                creature(new ResplendentAngel(), ownerId, "Resplendent Angel", "{1}{W}{W}", 3, 3,
                        CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.ANGEL),
                        Set.of(Keyword.FLYING), "Flying\nAt the beginning of each end step, if you gained 5 or more life this turn, create a 4/4 white Angel creature token with flying and vigilance."),
                steelPlumeMarshal(ownerId),
                creature(new DuelcraftTrainer(), ownerId, "Duelcraft Trainer", "{3}{W}", 3, 3,
                        CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                        Set.of(Keyword.FIRST_STRIKE), "First strike\nCoven — At the beginning of combat on your turn, target creature you control gains double strike until end of turn if you control three or more creatures with different powers."),
                creature(new FalconerAdept(), ownerId, "Falconer Adept", "{3}{W}", 2, 3,
                        CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                        Set.of(), "Whenever Falconer Adept attacks, create a 1/1 white Bird creature token with flying that's tapped and attacking."),
                creature(new SeraphOfDawn(), ownerId, "Seraph of Dawn", "{2}{W}{W}", 2, 4,
                        CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.ANGEL),
                        Set.of(Keyword.FLYING, Keyword.LIFELINK), "Flying, lifelink"),
                creature(new StarCrownedStag(), ownerId, "Star-Crowned Stag", "{3}{W}", 3, 3,
                        CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.ELK),
                        Set.of(), "Whenever Star-Crowned Stag attacks, tap target creature defending player controls."),
                creature(new BenalishMarshal(), ownerId, "Benalish Marshal", "{W}{W}{W}", 3, 3,
                        CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.HUMAN, CardSubtype.KNIGHT),
                        Set.of(), "Other creatures you control get +1/+1."),
                creature(new SerraParagon(), ownerId, "Serra Paragon", "{2}{W}{W}", 3, 4,
                        CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.ANGEL),
                        Set.of(Keyword.FLYING), "Flying\nDuring each of your turns, you may play a land from your graveyard or cast a permanent spell with mana value 3 or less from your graveyard. If you do, it gains \"When this permanent dies, exile it.\""),
                creature(new BladeHistorian(), ownerId, "Blade Historian", "{R/W}{R/W}{R/W}{R/W}", 2, 3,
                        CardColor.RED, List.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.HUMAN, CardSubtype.CLERIC),
                        Set.of(), "Attacking creatures you control have double strike."),
                creature(new CaptivatingCrew(), ownerId, "Captivating Crew", "{3}{R}", 4, 3,
                        CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.HUMAN, CardSubtype.PIRATE),
                        Set.of(), "{3}{R}: Gain control of target creature until end of turn. Untap it. It gains haste until end of turn. Activate only as a sorcery."),
                creature(new ManaformHellkite(), ownerId, "Manaform Hellkite", "{2}{R}{R}", 4, 4,
                        CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), "Flying\nWhenever you cast a noncreature spell, create an X/X red Dragon Illusion creature token with flying and haste, where X is the amount of mana spent to cast that spell. Exile that token at the beginning of the next end step."),
                creature(new MoonveilRegent(), ownerId, "Moonveil Regent", "{3}{R}", 4, 4,
                        CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), "Flying\nWhenever you cast a spell, you may discard your hand. If you do, draw a card for each of that spell's colors.\nWhen Moonveil Regent dies, it deals X damage to any target, where X is the number of colors among permanents you control."),
                creature(new SkyshipStalker(), ownerId, "Skyship Stalker", "{2}{R}{R}", 3, 3,
                        CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.CAT, CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), "Flying\n{R}: Skyship Stalker gets +1/+0 until end of turn.\n{R}: Skyship Stalker gains first strike until end of turn.\n{R}: Skyship Stalker gains haste until end of turn."),
                creature(new OgreBattledriver(), ownerId, "Ogre Battledriver", "{2}{R}{R}", 3, 3,
                        CardColor.RED, List.of(CardColor.RED), List.of(CardSubtype.OGRE, CardSubtype.WARRIOR),
                        Set.of(), "Whenever another creature enters the battlefield under your control, that creature gets +2/+0 and gains haste until end of turn.")));
    }

    private Card steelPlumeMarshal(UUID ownerId) {
        Card card = new Card();
        card.addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(2, 2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentHasKeywordPredicate(Keyword.FLYING),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())))));
        return creature(card, ownerId, "Steel-Plume Marshal", "{3}{W}{W}", 3, 3,
                CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardSubtype.BIRD, CardSubtype.SOLDIER),
                Set.of(Keyword.FLYING), "Flying\nWhenever this creature attacks, other attacking creatures you control with flying get +2/+2 until end of turn.");
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
        card.setToken(false);
        card.setTokenCard(false);
        card.freeze();
        return card;
    }
}
