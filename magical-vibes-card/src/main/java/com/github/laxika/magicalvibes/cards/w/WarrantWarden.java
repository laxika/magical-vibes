package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "230")
public class WarrantWarden extends Card {

    public WarrantWarden() {
        TargetFilter attackingOrBlockingCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentIsBlockingPredicate()
                        ))
                )),
                "Target must be an attacking or blocking creature."
        );
        CardEffect warrant = new PutTargetOnTopOfLibraryEffect();
        CardEffect warden = new CreateTokenEffect(
                1, "Sphinx", 4, 4, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLUE),
                List.of(CardSubtype.SPHINX),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of());

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Warrant — Put target attacking or blocking creature on top of its owner's library",
                        List.of(warrant),
                        List.of(attackingOrBlockingCreature)
                ).withManaCost("{W/U}{W/U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Warden — Create a 4/4 white and blue Sphinx creature token with flying and vigilance",
                        warden
                ).withManaCost("{3}{W}{U}")
        )));
    }
}
