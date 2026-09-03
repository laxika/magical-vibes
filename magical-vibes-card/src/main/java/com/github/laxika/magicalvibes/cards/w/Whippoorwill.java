package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageToTargetCreatureCantBePreventedOrRedirectedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "91")
public class Whippoorwill extends Card {

    public Whippoorwill() {
        addActivatedAbility(new ActivatedAbility(true, "{G}{G}", List.of(
                new DamageToTargetCreatureCantBePreventedOrRedirectedThisTurnEffect(),
                new PreventTargetCreatureRegenerationThisTurnEffect(),
                new ResolveEffectOnTargetDeathThisTurnEffect(
                        new ExileTriggeringCardFromGraveyardEffect())),
                "{G}{G}, {T}: Target creature can't be regenerated this turn. Damage that would be dealt to that creature this turn can't be prevented or dealt instead to another permanent or player. When the creature dies this turn, exile the creature."));
    }
}
