package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualIncorporationEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YSOS", collectorNumber = "22")
public class InspiringEasel extends Card {

    public InspiringEasel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaOfColorsEffect(
                        ManaColor.COLORS,
                        new ManaRestriction.SpellTypes(Set.of(CardType.INSTANT, CardType.SORCERY)))),
                "{T}: Add one mana of any color. Spend this mana only to cast an instant or sorcery spell."));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ChooseCardFromHandAndApplyPerpetualIncorporationEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        "{U}{R}",
                        new CopyNextInstantOrSorceryCastThisTurnEffect())),
                "{T}: Choose an instant or sorcery card in your hand. It perpetually incorporates {U}{R} "
                        + "and gains \"When you cast this spell, copy it. You may choose new targets for the copy.\" "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
