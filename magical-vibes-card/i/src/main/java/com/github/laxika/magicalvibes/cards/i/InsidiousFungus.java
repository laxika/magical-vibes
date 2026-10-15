package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "186")
public class InsidiousFungus extends Card {

    public InsidiousFungus() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Destroy target artifact",
                                        new DestroyTargetPermanentEffect(new PermanentIsArtifactPredicate()),
                                        TargetFilters.artifact()),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Destroy target enchantment",
                                        new DestroyTargetPermanentEffect(new PermanentIsEnchantmentPredicate()),
                                        TargetFilters.enchantment()),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Draw a card. Then you may put a land card from your hand onto the battlefield tapped",
                                        SequenceEffect.of(
                                                new DrawCardEffect(),
                                                new MayEffect(
                                                        new PutCardToBattlefieldEffect(
                                                                new CardTypePredicate(CardType.LAND), "land", true),
                                                        "Put a land card from your hand onto the battlefield tapped?")))
                        ))),
                "{2}, Sacrifice this creature: Choose one — Destroy target artifact; destroy target enchantment; or draw a card. Then you may put a land card from your hand onto the battlefield tapped."
        ).withModalChoiceAtActivation());
    }
}
