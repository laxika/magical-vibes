package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyOneColorInstantSorceryOnlyManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "221")
@CardRegistration(set = "SOS", collectorNumber = "355")
public class ResonatingLute extends Card {

    public ResonatingLute() {
        // Lands you control have "{T}: Add two mana of any one color. Spend this mana only to cast
        // instant and sorcery spells."
        ActivatedAbility landManaAbility = new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyOneColorInstantSorceryOnlyManaEffect(2)),
                "{T}: Add two mana of any one color. Spend this mana only to cast instant and sorcery spells."
        );
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                landManaAbility, GrantScope.OWN_PERMANENTS, new PermanentIsLandPredicate()));

        // {T}: Draw a card. Activate only if you have seven or more cards in your hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1)),
                "{T}: Draw a card. Activate only if you have seven or more cards in your hand."
        ).withMinCardsInHand(7));
    }
}
