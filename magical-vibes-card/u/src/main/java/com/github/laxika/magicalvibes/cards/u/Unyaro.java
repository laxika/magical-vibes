package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.PlaneswalkedToPlaneThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutCreaturesUntilPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "68")
public class Unyaro extends Card {

    public Unyaro() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new PlaneswalkedToPlaneThisTurn("Unyaro"),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES),
                                new PhaseOutCreaturesUntilPlaneswalkEffect())));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new CreateTokenEffect(
                2, "Knight", 2, 2, null,
                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.KNIGHT),
                Set.of(Keyword.VIGILANCE), Set.of()));
    }
}
