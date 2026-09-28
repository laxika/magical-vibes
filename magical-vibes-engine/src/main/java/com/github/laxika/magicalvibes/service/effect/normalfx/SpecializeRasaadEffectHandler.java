package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.r.RasaadMonkOfSelNe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeRasaadEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Applies Rasaad's five digital specialized faces to the permanent's runtime card. */
@Component
@RequiredArgsConstructor
public class SpecializeRasaadEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeRasaadEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeRasaadEffect specialize = (SpecializeRasaadEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Rasaad, Monk of Selûne".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        setBaseFaceCharacteristics(specialized);

        CardEffect specializedTrigger = RasaadMonkOfSelNe.specializedTrigger(specialize.color());
        switch (specialize.color()) {
            case WHITE -> {
                specialized.setName("Rasaad, Radiant Monk");
                specialized.setManaCost("{2}{W}{W}");
                specialized.setCardText("When this creature specializes, target creature card exiled with this creature perpetually loses all abilities and has base power and toughness 1/1.");
                specialized.setPower(4);
                specialized.setToughness(4);
                specialized.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, specializedTrigger);
            }
            case BLUE -> {
                specialized.setName("Rasaad, Dragon Monk");
                specialized.setManaCost("{2}{W}{U}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setCardText("When Rasaad, Dragon Monk dies, create two 1/1 blue Faerie Dragon creature tokens with flying.");
                specialized.setPower(4);
                specialized.setToughness(4);
                specialized.addEffect(EffectSlot.ON_DEATH, specializedTrigger);
            }
            case BLACK -> {
                specialized.setName("Rasaad, Shadow Monk");
                specialized.setManaCost("{2}{W}{B}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                specialized.setCardText("When Rasaad, Shadow Monk dies, create a 4/1 black Skeleton creature token with menace.");
                specialized.setPower(4);
                specialized.setToughness(4);
                specialized.addEffect(EffectSlot.ON_DEATH, specializedTrigger);
            }
            case RED -> {
                specialized.setName("Rasaad, Warrior Monk");
                specialized.setManaCost("{2}{R}{W}");
                specialized.setColors(List.of(CardColor.RED, CardColor.WHITE));
                specialized.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                specialized.setColor(CardColor.RED);
                specialized.setCardText("When Rasaad, Warrior Monk dies, create three 1/1 white Soldier creature tokens.");
                specialized.setPower(4);
                specialized.setToughness(4);
                specialized.addEffect(EffectSlot.ON_DEATH, specializedTrigger);
            }
            case GREEN -> {
                specialized.setName("Rasaad, Sylvan Monk");
                specialized.setManaCost("{2}{G}{W}");
                specialized.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                specialized.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                specialized.setColor(CardColor.GREEN);
                specialized.setCardText("When Rasaad, Sylvan Monk dies, create two 2/2 green Boar creature tokens.");
                specialized.setPower(4);
                specialized.setToughness(4);
                specialized.addEffect(EffectSlot.ON_DEATH, specializedTrigger);
            }
            default -> throw new IllegalStateException("Unsupported Rasaad specialization color: "
                    + specialize.color());
        }

        source.exchangeCard(specialized);
        if (specialize.color() == CardColor.WHITE) {
            queueSpecializationTrigger(gameData, entry, source, specialized, specializedTrigger);
        }
    }

    private void queueSpecializationTrigger(GameData gameData, StackEntry sourceEntry,
                                             Permanent source, Card sourceCard, CardEffect effect) {
        gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                sourceCard, sourceEntry.getControllerId(), List.of(effect), "specializes",
                sourceEntry.getSourcePermanentId(), new Permanent(source)));
    }

    private void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("4");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.MONK));
        card.setColor(CardColor.WHITE);
        card.setColors(List.of(CardColor.WHITE));
        card.setColorIdentity(List.of(CardColor.WHITE));
        card.setKeywords(Set.of());
    }
}
