package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.s.ShadowheartSharranCleric;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.SpecializeShadowheartEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Applies Shadowheart, Sharran Cleric's five digital specialized faces. */
@Component
@RequiredArgsConstructor
public class SpecializeShadowheartEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeShadowheartEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeShadowheartEffect specialize = (SpecializeShadowheartEffect) effect;
        var source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Shadowheart, Sharran Cleric".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        ShadowheartSharranCleric.setSpecializedBaseCharacteristics(specialized);
        specialized.setPower(4);
        specialized.setToughness(4);

        switch (specialize.color()) {
            case WHITE -> {
                specialized.setName("Shadowheart, Cleric of Order");
                specialized.setManaCost("{1}{W}{B}");
                specialized.setColors(List.of(CardColor.WHITE, CardColor.BLACK));
                specialized.setColorIdentity(List.of(CardColor.WHITE, CardColor.BLACK));
                specialized.setColor(CardColor.WHITE);
                specialized.setCardText("Deathtouch\nAt the beginning of your end step, Shadowheart, Cleric of Order deals 1 damage to each player.\nWhenever you lose life during your turn, create a 2/2 white Knight creature token.");
            }
            case BLUE -> {
                specialized.setName("Shadowheart, Cleric of Trickery");
                specialized.setManaCost("{1}{U}{B}");
                specialized.setColors(List.of(CardColor.BLUE, CardColor.BLACK));
                specialized.setColorIdentity(List.of(CardColor.BLUE, CardColor.BLACK));
                specialized.setColor(CardColor.BLUE);
                specialized.setCardText("Deathtouch\nAt the beginning of your end step, Shadowheart, Cleric of Trickery deals 1 damage to each player.\nWhenever you lose life during your turn, draw a card.");
            }
            case BLACK -> {
                specialized.setName("Shadowheart, Cleric of Graves");
                specialized.setManaCost("{1}{B}{B}");
                specialized.setKeywords(Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK));
                specialized.setCardText("Deathtouch, lifelink\nAt the beginning of your end step, Shadowheart, Cleric of Graves deals 1 damage to each player.");
            }
            case RED -> {
                specialized.setName("Shadowheart, Cleric of War");
                specialized.setManaCost("{1}{B}{R}");
                specialized.setColors(List.of(CardColor.BLACK, CardColor.RED));
                specialized.setColorIdentity(List.of(CardColor.BLACK, CardColor.RED));
                specialized.setColor(CardColor.RED);
                specialized.setCardText("Deathtouch\nAt the beginning of your end step, Shadowheart, Cleric of War deals 1 damage to each player.\nWhenever you lose life during your turn, Shadowheart deals that much damage to each opponent.");
            }
            case GREEN -> {
                specialized.setName("Shadowheart, Cleric of Twilight");
                specialized.setManaCost("{1}{B}{G}");
                specialized.setColors(List.of(CardColor.BLACK, CardColor.GREEN));
                specialized.setColorIdentity(List.of(CardColor.BLACK, CardColor.GREEN));
                specialized.setColor(CardColor.GREEN);
                specialized.setCardText("Deathtouch\nShadowheart, Cleric of Twilight can't be blocked by creatures with power 2 or less.\nAt the beginning of your end step, Shadowheart deals 1 damage to each player.\nWhenever you lose life during your turn, put a +1/+1 counter on Shadowheart.");
                specialized.addEffect(EffectSlot.STATIC,
                        new CantBeBlockedByCreaturesMatchingPredicateEffect(new PermanentPowerAtMostPredicate(2)));
            }
            default -> throw new IllegalStateException("Unsupported Shadowheart specialization color: "
                    + specialize.color());
        }

        specialized.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_PLAYER));
        CardEffect lifeLossTrigger = ShadowheartSharranCleric.specializedLifeLossTrigger(specialize.color());
        if (lifeLossTrigger != null) {
            specialized.addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE, lifeLossTrigger);
        }
        source.exchangeCard(specialized);
    }
}
