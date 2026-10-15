package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "270")
public class GideonMartialParagon extends Card {

    public GideonMartialParagon() {
        // +2: Untap all creatures you control. Those creatures get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()),
                        new BoostAllOwnCreaturesEffect(1, 1)
                ),
                "+2: Untap all creatures you control. Those creatures get +1/+1 until end of turn."
        ));

        // 0: Until end of turn, Gideon becomes a 5/5 Human Soldier creature with indestructible
        // that's still a planeswalker. Prevent all damage that would be dealt to him this turn.
        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(
                        new AnimatePermanentsEffect(5, 5, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                                Set.of(Keyword.INDESTRUCTIBLE)),
                        PreventDamageEffect.allToSelf()
                ),
                "0: Until end of turn, Gideon becomes a 5/5 Human Soldier creature with indestructible that's still a planeswalker. Prevent all damage that would be dealt to him this turn."
        ));

        // −10: Creatures you control get +2/+2 until end of turn. Tap all creatures your opponents control.
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(
                        new BoostAllOwnCreaturesEffect(2, 2),
                        new TapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))
                ),
                "\u221210: Creatures you control get +2/+2 until end of turn. Tap all creatures your opponents control."
        ));
    }
}
