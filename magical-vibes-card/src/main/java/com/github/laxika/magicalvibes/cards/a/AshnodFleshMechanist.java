package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "84")
public class AshnodFleshMechanist extends Card {

    public AshnodFleshMechanist() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        CreateTokenEffect.ofPowerstoneToken(new Fixed(1)),
                        "another creature"),
                "Sacrifice another creature?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        new CreateTokenEffect(
                                1, "Zombie", 3, 3, null,
                                List.of(CardSubtype.ZOMBIE), Set.of(), Set.of(CardType.ARTIFACT),
                                true
                        )
                ),
                "{5}, Exile a creature card from your graveyard: Create a tapped 3/3 colorless Zombie artifact creature token."
        ));
    }
}
