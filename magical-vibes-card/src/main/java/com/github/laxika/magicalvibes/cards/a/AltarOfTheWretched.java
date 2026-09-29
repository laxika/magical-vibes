package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WretchedBonemass;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentPower;
import com.github.laxika.magicalvibes.model.effect.CraftMaterialCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromExileTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "10")
@CardRegistration(set = "LCC", collectorNumber = "22")
public class AltarOfTheWretched extends Card {

    public AltarOfTheWretched() {
        setBackFaceCard(new WretchedBonemass());

        SacrificedPermanentPower sacrificedPower = new SacrificedPermanentPower();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate())
                        )),
                        SequenceEffect.of(
                                new DrawCardEffect(sacrificedPower),
                                new MillEffect(sacrificedPower, MillRecipient.CONTROLLER)),
                        "a nontoken creature"),
                "Sacrifice a nontoken creature?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(new ExileSelfCost(),
                        new CraftMaterialCost(1, CardType.CREATURE, null, List.of(), false, false, true),
                        new ReturnSourceFromExileTransformedEffect()),
                "{2}{B}{B}, Exile this artifact, Exile one or more creatures you control and/or creature cards "
                        + "from your graveyard: Return this card transformed under its owner's control. Craft only "
                        + "as a sorcery.",
                com.github.laxika.magicalvibes.model.ActivationTimingRestriction.SORCERY_SPEED));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{2}{B}: Return this card from your graveyard to your hand."));
    }

    @Override
    public String getBackFaceClassName() {
        return "WretchedBonemass";
    }
}
