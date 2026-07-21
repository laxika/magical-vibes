package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BriselaVoiceOfNightmares;
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

@CardRegistration(set = "INR", collectorNumber = "24")
public class GiselaTheBrokenBlade extends Card {

    private static final String BRUNA_NAME = "Bruna, the Fading Light";

    public GiselaTheBrokenBlade() {
        BriselaVoiceOfNightmares meldResult = new BriselaVoiceOfNightmares();
        meldResult.setSetCode(getSetCode());
        setBackFaceCard(meldResult);

        // At the beginning of your end step, if you both own and control Gisela and a creature
        // named Bruna, the Fading Light, exile them, then meld them into Brisela, Voice of Nightmares.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))),
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentNamedPredicate(BRUNA_NAME),
                                new PermanentIsCreaturePredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))))),
                new MeldWithNamedCreatureEffect(BRUNA_NAME)));
    }

    @Override
    public String getBackFaceClassName() {
        return "BriselaVoiceOfNightmares";
    }
}
