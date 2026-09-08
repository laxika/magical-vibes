package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "148")
public class GoblinWizardry extends Card {

    public GoblinWizardry() {
        Map<EffectSlot, CardEffect> tokenEffects = Map.of(
                EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new BoostSelfEffect(1, 1))));
        CreateTokenEffect goblinWizardToken = new CreateTokenEffect(
                CardType.CREATURE, 2, "Goblin Wizard", 1, 1,
                CardColor.RED, null, List.of(CardSubtype.GOBLIN, CardSubtype.WIZARD),
                Set.of(), Set.of(), false, false, tokenEffects, List.of(),
                false, false, false, 0, Set.of());

        addEffect(EffectSlot.SPELL, goblinWizardToken);
    }
}
