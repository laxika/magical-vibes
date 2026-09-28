package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SeekThreeNonlandPermanentAndChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeKlementEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Applies Klement's digital specialized faces to the permanent's runtime card. */
@Component
@RequiredArgsConstructor
public class SpecializeKlementEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeKlementEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeKlementEffect specialize = (SpecializeKlementEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Klement, Novice Acolyte".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        setBaseFaceCharacteristics(specialized, specialize.color());
        switch (specialize.color()) {
            case BLACK -> {
                specialized.setName("Klement, Death Acolyte");
                specialized.setManaCost("{1}{W}{B}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                specialized.setColor(CardColor.WHITE);
                specialized.setCardText("When this creature specializes, create two 2/2 black Zombie creature tokens.");
                specialized.setPower(3);
                specialized.setToughness(3);
            }
            case BLUE -> {
                specialized.setName("Klement, Knowledge Acolyte");
                specialized.setManaCost("{1}{W}{U}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColor(CardColor.WHITE);
                specialized.setKeywords(Set.of(Keyword.VIGILANCE));
                specialized.setCardText("Vigilance\nWhen this creature specializes, seek three nonland permanent cards. Choose one of those cards and shuffle the rest into your library.");
                specialized.setPower(3);
                specialized.setToughness(4);
            }
            case WHITE -> {
                specialized.setName("Klement, Life Acolyte");
                specialized.setManaCost("{1}{W}{W}");
                specialized.setCardText("Lifelink\nWhen this creature specializes, you get a one-time boon with \"When you cast a creature spell, that creature enters with a lifelink counter on it.\"");
                specialized.setKeywords(Set.of(Keyword.LIFELINK));
                specialized.setPower(3);
                specialized.setToughness(3);
            }
            case RED -> {
                specialized.setName("Klement, Tempest Acolyte");
                specialized.setManaCost("{1}{R}{W}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.RED));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.RED));
                specialized.setColor(CardColor.WHITE);
                specialized.setKeywords(Set.of(Keyword.DOUBLE_STRIKE));
                specialized.setCardText("Double strike\nWhenever Klement, Tempest Acolyte is dealt damage, it deals that much damage to any target.");
                specialized.setPower(2);
                specialized.setToughness(3);
                specialized.addEffect(EffectSlot.ON_DEALT_DAMAGE,
                        new DealDamageToAnyTargetEffect(new EventValue()));
            }
            case GREEN -> {
                specialized.setName("Klement, Nature Acolyte");
                specialized.setManaCost("{1}{G}{W}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.GREEN));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.GREEN));
                specialized.setColor(CardColor.WHITE);
                specialized.setCardText("When Klement, Nature Acolyte leaves the battlefield, create a 4/4 green Ox creature token.");
                specialized.setPower(4);
                specialized.setToughness(4);
                specialized.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new CreateTokenEffect(CardType.CREATURE, 1, "Ox", 4, 4, CardColor.GREEN, null,
                                List.of(CardSubtype.OX), Set.of(), Set.of(), false, false, java.util.Map.of(),
                                List.of(), false, false, false, 0, Set.of()));
            }
            default -> throw new IllegalStateException("Unsupported Klement specialization color: "
                    + specialize.color());
        }
        source.exchangeCard(specialized);
        if (specialize.color() == CardColor.WHITE) {
            gameData.boons.add(new Boon(entry.getControllerId(), specialized,
                    new PutCountersOnEnteringCreatureEffect(CounterType.LIFELINK, 1, false), 1));
        } else if (specialize.color() == CardColor.BLACK) {
            enqueueSpecializationTrigger(gameData, entry, specialized, CreateTokenEffect.blackZombie(2));
        } else if (specialize.color() == CardColor.BLUE) {
            enqueueSpecializationTrigger(gameData, entry, specialized,
                    new SeekThreeNonlandPermanentAndChooseOneEffect());
        }
    }

    private void enqueueSpecializationTrigger(GameData gameData, StackEntry sourceEntry, Card sourceCard,
                                               CardEffect effect) {
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard,
                sourceEntry.getControllerId(), sourceCard.getName() + "'s ability", List.of(effect), 0,
                sourceEntry.getSourcePermanentId());
        trigger.setNonTargeting(true);
        gameData.enqueueTrigger(trigger);
    }

    private void setBaseFaceCharacteristics(Card card, CardColor chosenColor) {
        card.setSetCode("HBG");
        card.setCollectorNumber("1");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.TIEFLING, CardSubtype.CLERIC));
        card.setColor(chosenColor);
        card.setColors(List.of(chosenColor));
        card.setColorIdentity(List.of(chosenColor));
        card.setKeywords(Set.of());
    }
}
