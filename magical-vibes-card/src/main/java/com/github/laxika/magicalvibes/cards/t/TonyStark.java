package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "80")
public class TonyStark extends Card {

    public TonyStark() {
        setBackFaceCard(new TheInvincibleIronMan());
        setModalDoubleFaced(true);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        4, new CardTypePredicate(CardType.ARTIFACT))),
                "{1}, {T}: Look at the top four cards of your library. You may reveal an artifact card from among them and put it into your hand. Put the rest on the bottom of your library in a random order."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{R}",
                List.of(new TransformSelfEffect()),
                "{4}{U}{R}: Transform Tony Stark. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Tony Stark", List.of())
                        .withManaCost("{1}{U}"),
                new ChooseOneEffect.ChooseOneOption("The Invincible Iron Man", List.of())
                        .withManaCost("{4}{U}{R}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheInvincibleIronMan";
    }
}
