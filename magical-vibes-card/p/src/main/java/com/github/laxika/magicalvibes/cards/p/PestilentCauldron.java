package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RestorativeBurst;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "154")
public class PestilentCauldron extends Card {

    public PestilentCauldron() {
        RestorativeBurst backFace = new RestorativeBurst();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DiscardCardTypeCost(null, null), pestToken()),
                "{T}, Discard a card: Create a 1/1 black and green Pest creature token with \"When this token dies, you gain 1 life.\""
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MillEffect(new LifeGainedThisTurn(CountScope.CONTROLLER), MillRecipient.EACH_OPPONENT)),
                "{1}, {T}: Each opponent mills cards equal to the amount of life you gained this turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(4, null, false),
                        new DrawCardEffect()),
                "{4}, {T}: Exile four target cards from a single graveyard. Draw a card.",
                List.of(graveyardCardTarget(), graveyardCardTarget(), graveyardCardTarget(), graveyardCardTarget()),
                4,
                4
        ));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Pestilent Cauldron", List.of()),
                new ChooseOneEffect.ChooseOneOption(
                        "Restorative Burst", backFace.getEffects(EffectSlot.SPELL)))));
    }

    private static CreateTokenEffect pestToken() {
        return new CreateTokenEffect(
                CardType.CREATURE, 1, "Pest", 1, 1,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.GREEN),
                List.of(CardSubtype.PEST), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_DEATH, new GainLifeEffect(1)),
                List.of(), false, false, false, 0, Set.of());
    }

    private static TargetFilter graveyardCardTarget() {
        return new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS);
    }

    @Override
    public String getBackFaceClassName() {
        return "RestorativeBurst";
    }
}
