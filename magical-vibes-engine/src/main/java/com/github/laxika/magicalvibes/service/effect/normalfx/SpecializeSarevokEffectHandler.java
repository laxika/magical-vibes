package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.s.SarevokTheUsurper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeSarevokEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Applies Sarevok the Usurper's five digital specialized faces. */
@Component
@RequiredArgsConstructor
public class SpecializeSarevokEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeSarevokEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeSarevokEffect specialize = (SpecializeSarevokEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Sarevok the Usurper".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        SarevokTheUsurper.setSpecializedBaseCharacteristics(specialized);
        specialized.setPower(4);
        specialized.setToughness(4);
        setFaceCharacteristics(specialized, specialize.color());

        specialized.target(TargetFilters.creatureYouControl()).addEffect(
                EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                SarevokTheUsurper.specializedCombatTrigger(specialize.color()));
        source.exchangeCard(specialized);

        if (specialize.color() == CardColor.BLACK) {
            enqueueSpecializationTrigger(gameData, entry, specialized,
                    SarevokTheUsurper.specializationTrigger());
        }
    }

    private void enqueueSpecializationTrigger(GameData gameData, StackEntry sourceEntry,
                                               Card sourceCard, CardEffect effect) {
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                sourceEntry.getControllerId(),
                sourceCard.getName() + "'s ability",
                List.of(effect),
                0,
                sourceEntry.getSourcePermanentId());
        trigger.setNonTargeting(true);
        gameData.enqueueTrigger(trigger);
    }

    private void setFaceCharacteristics(Card card, CardColor color) {
        switch (color) {
            case WHITE -> {
                card.setName("Sarevok, Divine Usurper");
                card.setManaCost("{3}{W}{B}");
                card.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                card.setColor(CardColor.WHITE);
                card.setKeywords(Set.of(Keyword.FIRST_STRIKE));
                card.setCardText("First strike\nAt the beginning of combat on your turn, target creature you control gains first strike and gets +X/+0 until end of turn, where X is the number of creature cards in your graveyard.");
            }
            case BLUE -> {
                card.setName("Sarevok, Deceitful Usurper");
                card.setManaCost("{3}{U}{B}");
                card.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                card.setColor(CardColor.BLUE);
                card.setCardText("At the beginning of combat on your turn, target creature you control gets +X/+0 until end of turn, where X is the number of creature, instant, and sorcery cards in your graveyard.");
            }
            case BLACK -> {
                card.setName("Sarevok, Deadly Usurper");
                card.setManaCost("{3}{B}{B}");
                card.setCardText("When this creature specializes, seek a creature card and put it into your graveyard, then conjure two duplicates of it into your graveyard.\nAt the beginning of combat on your turn, target creature you control gets +X/+0 until end of turn, where X is the number of creature cards in your graveyard.");
            }
            case RED -> {
                card.setName("Sarevok, Ferocious Usurper");
                card.setManaCost("{3}{B}{R}");
                card.setColors(List.of(CardColor.RED, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.RED, CardColor.BLACK));
                card.setColor(CardColor.RED);
                card.setKeywords(Set.of(Keyword.MENACE));
                card.setCardText("Menace\nAt the beginning of combat on your turn, target creature you control gains menace and gets +X/+0 until end of turn, where X is the number of creature cards in your graveyard.");
            }
            case GREEN -> {
                card.setName("Sarevok, Mighty Usurper");
                card.setManaCost("{3}{B}{G}");
                card.setColors(List.of(CardColor.GREEN, CardColor.BLACK));
                card.setColorIdentity(List.of(CardColor.GREEN, CardColor.BLACK));
                card.setColor(CardColor.GREEN);
                card.setKeywords(Set.of(Keyword.TRAMPLE));
                card.setCardText("Trample\nAt the beginning of combat on your turn, target creature you control gains trample and gets +X/+0 until end of turn, where X is the number of creature cards in your graveyard.");
            }
        }
    }
}
