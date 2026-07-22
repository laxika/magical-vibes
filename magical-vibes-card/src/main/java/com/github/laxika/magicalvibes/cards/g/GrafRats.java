package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "113")
public class GrafRats extends Card {

    private static final String PARTNER_NAME = "Midnight Scavengers";

    public GrafRats() {
        ChitteringHost meldResult = new ChitteringHost();
        meldResult.setSetCode(getSetCode());
        setBackFaceCard(meldResult);

        // At the beginning of combat on your turn, if you both own and control this creature and a
        // creature named Midnight Scavengers, exile them, then meld them into Chittering Host.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))),
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentNamedPredicate(PARTNER_NAME),
                                new PermanentIsCreaturePredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))))),
                new MeldWithNamedCreatureEffect(PARTNER_NAME)));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChitteringHost";
    }
}
