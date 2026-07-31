package com.github.laxika.magicalvibes.model.effect;

/**
 * "At the beginning of your upkeep, you may sacrifice a Forest. If you sacrifice a snow Forest this
 * way, this creature gains trample until end of turn. If you don't sacrifice a Forest, sacrifice
 * this creature and it deals 7 damage to you." (Gargantuan Gorilla)
 *
 * <p>Not a {@link ForcedCostOrElseEffect}: paying the cost has a conditional <em>reward</em> (trample,
 * but only when the sacrificed Forest is snow), which that effect's fallback-only plumbing cannot
 * express. At resolution the controller is asked whether to sacrifice; accepting sacrifices one
 * Forest they control (choosing which when several qualify) and grants the source trample until end
 * of turn if that Forest was snow. Declining — or controlling no Forest, in which case no prompt is
 * shown — sacrifices the source and deals 7 damage to its controller.
 */
public record GargantuanGorillaUpkeepEffect() implements CardEffect {
}
