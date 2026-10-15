package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RagnarokDivineDeliverance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "211")
@CardRegistration(set = "FIN", collectorNumber = "392")
@CardRegistration(set = "FIN", collectorNumber = "475")
@CardRegistration(set = "FIN", collectorNumber = "537")
public class VanilleCheerfulLCie extends Card {

    private static final String PARTNER_NAME = "Fang, Fearless l'Cie";

    public VanilleCheerfulLCie() {
        setBackFaceCard(new RagnarokDivineDeliverance());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new MillEffect(2, MillRecipient.CONTROLLER),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsPermanentPredicate())
                        .mandatory(true)
                        .build()));

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))),
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentNamedPredicate(PARTNER_NAME),
                                new PermanentIsCreaturePredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))))),
                new MayPayManaEffect("{3}{B}{G}",
                        new MeldWithNamedCreatureEffect(PARTNER_NAME),
                        "Pay {3}{B}{G} to meld Vanille and Fang?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "RagnarokDivineDeliverance";
    }
}
