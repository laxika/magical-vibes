package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.l.LaezelGithyankiWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeLaezelEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Applies Lae'zel's five digital specialized faces to the permanent's runtime card. */
@Component
@RequiredArgsConstructor
public class SpecializeLaezelEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeLaezelEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var specialize = (SpecializeLaezelEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Lae'zel, Githyanki Warrior".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        setBaseFaceCharacteristics(specialized);

        CardEffect specializedTrigger = LaezelGithyankiWarrior.specializedTrigger(specialize.color());
        switch (specialize.color()) {
            case WHITE -> {
                specialized.setName("Lae'zel, Blessed Warrior");
                specialized.setManaCost("{3}{W}{W}");
                specialized.setCardText("Double strike\nWhen this creature enters or specializes, seek a nonland permanent card with mana value 3 or less.");
                specialized.setPower(3);
                specialized.setToughness(6);
            }
            case BLUE -> {
                specialized.setName("Lae'zel, Illithid Thrall");
                specialized.setManaCost("{3}{W}{U}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLUE));
                specialized.setSubtypes(List.of(CardSubtype.GITH, CardSubtype.HORROR, CardSubtype.WARRIOR));
                specialized.setCardText("Double strike\nWhen this creature enters or specializes, conjure a duplicate of a random creature card from an opponent's library into your hand. It perpetually gains \"You may spend mana as though it were mana of any color to cast this spell.\"");
                specialized.setPower(3);
                specialized.setToughness(6);
            }
            case BLACK -> {
                specialized.setName("Lae'zel, Callous Warrior");
                specialized.setManaCost("{3}{W}{B}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                specialized.setCardText("Double strike\nWhen this creature enters or specializes, return up to two target creature cards with total mana value 3 or less from your graveyard to the battlefield.");
                specialized.setPower(3);
                specialized.setToughness(6);
            }
            case RED -> {
                specialized.setName("Lae'zel, Wrathful Warrior");
                specialized.setManaCost("{3}{R}{W}");
                specialized.setColors(List.of(CardColor.RED, CardColor.WHITE));
                specialized.setColorIdentity(List.of(CardColor.RED, CardColor.WHITE));
                specialized.setColor(CardColor.RED);
                specialized.setCardText("Double strike\nWhen this creature enters or specializes, create two 1/1 white Soldier creature tokens.");
                specialized.setPower(3);
                specialized.setToughness(6);
            }
            case GREEN -> {
                specialized.setName("Lae'zel, Primal Warrior");
                specialized.setManaCost("{3}{G}{W}");
                specialized.setColors(List.of(CardColor.GREEN, CardColor.WHITE));
                specialized.setColorIdentity(List.of(CardColor.GREEN, CardColor.WHITE));
                specialized.setColor(CardColor.GREEN);
                specialized.setCardText("Double strike\nWhen this creature enters or specializes, other creatures you control and creature cards in your hand perpetually get +1/+1.");
                specialized.setPower(3);
                specialized.setToughness(6);
            }
            default -> throw new IllegalStateException("Unsupported Lae'zel specialization color: "
                    + specialize.color());
        }

        specialized.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, specializedTrigger);
        source.exchangeCard(specialized);
        enqueueSpecializationTrigger(gameData, entry, specialized, specializedTrigger);
    }

    private void enqueueSpecializationTrigger(GameData gameData, StackEntry sourceEntry, Card sourceCard,
                                              CardEffect effect) {
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard,
                sourceEntry.getControllerId(), sourceCard.getName() + "'s ability", List.of(effect), 0,
                sourceEntry.getSourcePermanentId());
        trigger.setNonTargeting(effect.targetSpec() == TargetSpec.NONE);
        gameData.enqueueTrigger(trigger);
    }

    private void setBaseFaceCharacteristics(Card card) {
        card.setSetCode("HBG");
        card.setCollectorNumber("2");
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of());
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.GITH, CardSubtype.WARRIOR));
        card.setColor(CardColor.WHITE);
        card.setColors(List.of(CardColor.WHITE));
        card.setColorIdentity(List.of(CardColor.WHITE));
        card.setKeywords(Set.of(Keyword.DOUBLE_STRIKE));
    }
}
