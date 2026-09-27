package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OtherControlledTokensBecomeCopyOfChosenTokenUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "2XM", collectorNumber = "193")
@CardRegistration(set = "MUL", collectorNumber = "36")
@CardRegistration(set = "MUL", collectorNumber = "101")
@CardRegistration(set = "MUL", collectorNumber = "166")
@CardRegistration(set = "SOC", collectorNumber = "299")
public class BrudicladTelchorEngineer extends Card {

    public BrudicladTelchorEngineer() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HASTE, GrantScope.OWN_CREATURES, new PermanentIsTokenPredicate()));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new CreateTokenEffect(
                        1, "Phyrexian Myr", 2, 1, CardColor.BLUE,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT)),
                new MayEffect(
                        new OtherControlledTokensBecomeCopyOfChosenTokenUntilEndOfTurnEffect(),
                        "Choose a token you control for your other tokens to copy?")));
    }
}
